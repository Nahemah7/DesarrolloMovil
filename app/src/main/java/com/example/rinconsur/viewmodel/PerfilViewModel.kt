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

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val persona: Colaborador? = repositorio.colaboradorPorId(sesion.leer())

    private var nombreOriginal: String = persona?.nombre ?: ""
    private var apellidoOriginal: String = persona?.apellido ?: ""

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
                    apellido = p.apellido,
                    rut = p.rut,
                    cargo = p.cargo,
                    perfil = p.perfil.etiqueta,
                    foto = repositorio.fotoDe(p.id),
                    catalogo = repositorio.preguntas(),
                    // La app usa una sola pregunta: si venia con dos guardadas se toma la primera.
                    elegidas = guardadas.take(1).map { q -> q.id },
                    tienePreguntas = guardadas.isNotEmpty()
                )
            }
        }
    }


    fun onNombreCambia(valor: String) {
        _uiState.update { it.copy(nombre = valor, errorNombre = null, avisoNombre = null) }
    }

    fun onApellidoCambia(valor: String) {
        _uiState.update { it.copy(apellido = valor, errorNombre = null, avisoNombre = null) }
    }

    fun guardarNombre(): Boolean {
        val p = persona ?: return false
        val e = _uiState.value
        val nombre = e.nombre.trim()
        val apellido = e.apellido.trim()
        if (!textoDeNombreValido(nombre)) {
            _uiState.update { it.copy(errorNombre = "Escribe el nombre con letras, de 2 a 40.") }
            return false
        }
        if (apellido.isNotEmpty() && !textoDeNombreValido(apellido)) {
            _uiState.update { it.copy(errorNombre = "Escribe el apellido con letras, de 2 a 40.") }
            return false
        }
        if (!repositorio.cambiarNombre(p.id, nombre, apellido)) {
            _uiState.update { it.copy(errorNombre = "No se pudo guardar el nombre.") }
            return false
        }
        nombreOriginal = nombre
        apellidoOriginal = apellido
        _uiState.update { it.copy(nombre = nombre, apellido = apellido, errorNombre = null, avisoNombre = "Tu nombre quedó guardado.") }
        return true
    }

    fun cancelarCambioNombre() {
        _uiState.update {
            it.copy(
                nombre = nombreOriginal,
                apellido = apellidoOriginal,
                errorNombre = null,
                avisoNombre = null
            )
        }
    }

    private fun textoDeNombreValido(texto: String): Boolean =
        texto.length in 2..40 && texto.all { it.isLetter() || it == ' ' || it == '-' || it == '\'' }


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


    fun onClaveActualCambia(v: String) { _uiState.update { it.copy(claveActual = v, errorActual = null, avisoClave = null) } }
    fun onClaveNuevaCambia(v: String) { _uiState.update { it.copy(claveNueva = v, errorNueva = null, avisoClave = null) } }
    fun onRepetirCambia(v: String) { _uiState.update { it.copy(repetirNueva = v, errorRepetir = null, avisoClave = null) } }
    fun onAlternarClave() { _uiState.update { it.copy(claveVisible = !it.claveVisible) } }

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


    fun onElegirPregunta(idPregunta: Long) {
        _uiState.update {
            it.copy(elegidas = listOf(idPregunta), errorPreguntas = null, avisoPreguntas = null)
        }
    }

    fun onRespuestaCambia(idPregunta: Long, valor: String) {
        _uiState.update {
            it.copy(respuestas = it.respuestas + (idPregunta to valor), errorPreguntas = null, avisoPreguntas = null)
        }
    }

    fun guardarPreguntas(): Boolean {
        val p = persona ?: return false
        val e = _uiState.value
        if (e.elegidas.size != 1) {
            _uiState.update { it.copy(errorPreguntas = "Elige una pregunta.") }
            return false
        }
        val pares = e.elegidas.map { id -> id to (e.respuestas[id] ?: "") }
        if (pares.any { (_, r) -> r.isBlank() }) {
            _uiState.update { it.copy(errorPreguntas = "Responde la pregunta.") }
            return false
        }
        repositorio.guardarRespuestas(p.id, p.rut, pares)
        _uiState.update {
            it.copy(tienePreguntas = true, respuestas = emptyMap(), avisoPreguntas = "Tu pregunta quedó guardada.")
        }
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
