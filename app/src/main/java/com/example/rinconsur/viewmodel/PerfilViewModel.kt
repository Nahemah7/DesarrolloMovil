package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Clave
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.mensajeDeClave
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Perfil de quien esta usando el telefono: sus datos y la seguridad de su cuenta —cambiar la clave
 * y elegir las dos preguntas con las que la recupera si se le olvida.
 */
class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val persona: Colaborador? = repositorio.colaboradorPorId(sesion.leer())

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        val p = persona
        if (p == null) {
            _uiState.update { it.copy(nombre = "Sin sesión") }
        } else {
            val guardadas = repositorio.preguntasDe(p.id)
            _uiState.update {
                it.copy(
                    nombre = p.nombre,
                    rut = p.rut,
                    cargo = p.cargo,
                    perfil = p.perfil.etiqueta,
                    foto = repositorio.fotoDe(p.id),
                    catalogo = repositorio.preguntas(),
                    elegidas = guardadas.map { q -> q.id },
                    tienePreguntas = guardadas.isNotEmpty()
                )
            }
        }
    }

    // ---------- foto de perfil ----------

    /** La foto ya viene recortada cuadrada y reducida desde la camara: aca solo se guarda. */
    fun guardarFoto(bytes: ByteArray?) {
        val p = persona ?: return
        if (repositorio.guardarFoto(p.id, bytes)) {
            _uiState.update { it.copy(foto = bytes, avisoFoto = "Tu foto quedó guardada.") }
        } else {
            _uiState.update { it.copy(avisoFoto = "No se pudo guardar la foto.") }
        }
    }

    fun onPermisoDenegado() {
        _uiState.update { it.copy(avisoFoto = "Sin permiso de cámara no se puede tomar la foto.") }
    }

    fun limpiarAvisoFoto() { _uiState.update { it.copy(avisoFoto = null) } }

    // ---------- cambio de clave ----------

    fun onClaveActualCambia(v: String) { _uiState.update { it.copy(claveActual = v, errorActual = null, avisoClave = null) } }
    fun onClaveNuevaCambia(v: String) { _uiState.update { it.copy(claveNueva = v, errorNueva = null, avisoClave = null) } }
    fun onRepetirCambia(v: String) { _uiState.update { it.copy(repetirNueva = v, errorRepetir = null, avisoClave = null) } }
    fun onAlternarClave() { _uiState.update { it.copy(claveVisible = !it.claveVisible) } }

    /**
     * Cambia la clave. Valida por campo: primero la actual, despues la regla acordada (8 caracteres
     * con mayuscula, minuscula y numero) y por ultimo que las dos nuevas coincidan.
     */
    fun cambiarClave(): Boolean {
        val p = persona
        if (p == null) {
            _uiState.update { it.copy(errorActual = "No se pudo leer tu sesión. Vuelve a entrar.") }
            return false
        }
        val e = _uiState.value
        if (Clave.hash(p.rut, e.claveActual) != p.passwordHash) {
            _uiState.update { it.copy(errorActual = "La clave actual no coincide.") }
            return false
        }
        mensajeDeClave(e.claveNueva)?.let {
            _uiState.update { s -> s.copy(errorNueva = it) }
            return false
        }
        if (e.claveNueva != e.repetirNueva) {
            _uiState.update { it.copy(errorRepetir = "Las dos claves no coinciden.") }
            return false
        }
        repositorio.cambiarClave(p.id, Clave.hash(p.rut, e.claveNueva))
        _uiState.update {
            it.copy(
                claveActual = "", claveNueva = "", repetirNueva = "",
                avisoClave = "Tu clave quedó cambiada."
            )
        }
        return true
    }

    // ---------- preguntas de seguridad ----------

    /** Marca o desmarca una pregunta. El caso pide dos: no deja elegir una tercera. */
    fun onAlternarPregunta(idPregunta: Long): String? {
        val e = _uiState.value
        val nuevas = e.elegidas.toMutableList()
        if (nuevas.contains(idPregunta)) {
            nuevas.remove(idPregunta)
        } else {
            if (nuevas.size >= 2) return "Elige solo dos preguntas."
            nuevas.add(idPregunta)
        }
        _uiState.update { it.copy(elegidas = nuevas, errorPreguntas = null, avisoPreguntas = null) }
        return null
    }

    fun onRespuestaCambia(idPregunta: Long, valor: String) {
        _uiState.update {
            it.copy(respuestas = it.respuestas + (idPregunta to valor), errorPreguntas = null, avisoPreguntas = null)
        }
    }

    /** Guarda las dos respuestas. Se guardan hasheadas, con el RUT como sal, igual que la clave. */
    fun guardarPreguntas(): Boolean {
        val p = persona ?: return false
        val e = _uiState.value
        if (e.elegidas.size != 2) {
            _uiState.update { it.copy(errorPreguntas = "Elige dos preguntas.") }
            return false
        }
        val pares = e.elegidas.map { id -> id to (e.respuestas[id] ?: "") }
        if (pares.any { (_, r) -> r.isBlank() }) {
            _uiState.update { it.copy(errorPreguntas = "Responde las dos preguntas.") }
            return false
        }
        repositorio.guardarRespuestas(p.id, p.rut, pares)
        _uiState.update {
            it.copy(tienePreguntas = true, respuestas = emptyMap(), avisoPreguntas = "Tus preguntas quedaron guardadas.")
        }
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
