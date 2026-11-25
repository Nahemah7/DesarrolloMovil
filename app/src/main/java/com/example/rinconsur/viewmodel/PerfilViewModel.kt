package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Clave
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.mensajeDeClave
import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionCambioClave
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PerfilUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PerfilViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)
    private val persona: Colaborador? = repositorio.colaboradorPorId(sesion.leer())

    private var cambiandoClave = false

    private var nombreOriginal: String = persona?.nombre ?: ""
    private var apellidoOriginal: String = persona?.apellido ?: ""

    val puedeExportarMetricas: Boolean = persona?.perfil == PerfilUsuario.JEFE_TIENDA ||
        persona?.perfil == PerfilUsuario.SUBGERENTE

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
                    perfil = p.cargo,
                    telefono = p.telefono,
                    foto = repositorio.fotoDe(p.id),
                    catalogo = repositorio.preguntas(),

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

    fun cambiarClave(onListo: (Boolean) -> Unit) {
        if (cambiandoClave) return
        val p = persona
        if (p == null) {
            _uiState.update { it.copy(errorActual = "No se pudo leer tu sesión. Vuelve a entrar.") }
            onListo(false)
            return
        }
        val e = _uiState.value
        if (Clave.hash(p.rut, e.claveActual) != p.passwordHash) {
            _uiState.update { it.copy(errorActual = "La clave actual no coincide.") }
            onListo(false)
            return
        }
        mensajeDeClave(e.claveNueva)?.let {
            _uiState.update { s -> s.copy(errorNueva = it) }
            onListo(false)
            return
        }
        if (e.claveNueva != e.repetirNueva) {
            _uiState.update { it.copy(errorRepetir = "Las dos claves no coinciden.") }
            onListo(false)
            return
        }
        val token = tokens.leer()
        if (token.isNullOrBlank()) {
            _uiState.update {
                it.copy(avisoClave = "Necesitas conexión con el servidor para cambiar la clave.")
            }
            onListo(false)
            return
        }

        cambiandoClave = true
        TrabajoDeFondo.alcance.launch {
            var aviso: String
            var ok = false
            try {
                val respuesta = ClienteRemoto(direccion.leer()).autenticacion.cambiarClave(
                    "Bearer " + token,
                    PeticionCambioClave(e.claveNueva)
                )
                ok = respuesta.isSuccessful
                aviso = when {
                    ok -> "Tu clave quedó cambiada."
                    respuesta.code() == 400 -> "La clave no cumple los requisitos: revísala."
                    respuesta.code() == 401 -> "Tu sesión venció. Vuelve a entrar."
                    else -> "El servidor respondió " + respuesta.code() + ": la clave no se cambió."
                }
            } catch (ex: Exception) {
                aviso = "Sin conexión con el servidor: la clave no se cambió."
            }
            if (ok) {
                repositorio.cambiarClave(p.id, Clave.hash(p.rut, e.claveNueva))
            }
            withContext(Dispatchers.Main) {
                cambiandoClave = false
                _uiState.update {
                    if (ok) it.copy(
                        claveActual = "", claveNueva = "", repetirNueva = "",
                        errorActual = null, errorNueva = null, errorRepetir = null,
                        avisoClave = aviso
                    ) else it.copy(avisoClave = aviso)
                }
                onListo(ok)
            }
        }
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
