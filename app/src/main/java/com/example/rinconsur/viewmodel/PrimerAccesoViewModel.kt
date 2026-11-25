package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionCambioClave
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.PrimerAccesoUiState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PrimerAccesoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private var guardando = false

    private val idUsuario: Long = sesion.leer()
    private val persona = repositorio.colaboradorPorId(idUsuario)

    val nombre: String = persona?.nombre ?: ""

    private val _uiState = MutableStateFlow(PrimerAccesoUiState(catalogo = repositorio.preguntas()))
    val uiState: StateFlow<PrimerAccesoUiState> = _uiState.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    fun onClaveCambia(valor: String) = _uiState.update { it.copy(clave = valor, errorClave = null) }

    fun onRepetirCambia(valor: String) = _uiState.update { it.copy(repetir = valor, errorRepetir = null) }

    fun onPreguntaElegida(idPregunta: Long) =
        _uiState.update { it.copy(idPregunta = idPregunta, errorPregunta = null) }

    fun onRespuestaCambia(valor: String) = _uiState.update { it.copy(respuesta = valor, errorRespuesta = null) }

    fun guardar(onListo: (PerfilUsuario?) -> Unit) {
        if (guardando) return
        val persona = persona ?: run {
            _aviso.tryEmit("No encontramos tu cuenta. Vuelve a entrar.")
            onListo(null)
            return
        }
        val revisado = _uiState.value.validar()
        _uiState.value = revisado
        if (!revisado.puedeGuardar) {
            onListo(null)
            return
        }
        val token = tokens.leer()
        if (token.isNullOrBlank()) {
            _aviso.tryEmit("Necesitas conexión con el servidor para guardar la clave. Revísala y vuelve a intentar.")
            onListo(null)
            return
        }

        guardando = true
        TrabajoDeFondo.alcance.launch {
            var mensaje: String? = null
            var perfil: PerfilUsuario? = null
            try {
                val respuesta = ClienteRemoto(direccion.leer()).autenticacion.cambiarClave(
                    "Bearer " + token,
                    PeticionCambioClave(revisado.clave)
                )
                when {
                    respuesta.isSuccessful -> {
                        val guardado = repositorio.definirClaveDefinitiva(
                            idUsuario = persona.id,
                            rut = persona.rut,
                            clave = revisado.clave,
                            idPregunta = revisado.idPregunta!!,
                            respuesta = revisado.respuesta
                        )
                        if (guardado) perfil = persona.perfil
                        else mensaje = "No pudimos guardar en el teléfono. Inténtalo otra vez."
                    }
                    respuesta.code() == 400 -> mensaje = "La clave no cumple los requisitos: revísala y vuelve a intentar."
                    respuesta.code() == 401 -> mensaje = "Tu sesión venció. Vuelve a entrar."
                    else -> mensaje = "El servidor respondió " + respuesta.code() + ": la clave no se cambió."
                }
            } catch (e: Exception) {
                mensaje = "Sin conexión con el servidor: la clave no se cambió."
            }
            withContext(Dispatchers.Main) {
                guardando = false
                mensaje?.let { _aviso.tryEmit(it) }
                onListo(perfil)
            }
        }
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
