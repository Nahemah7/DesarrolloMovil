package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.PrimerAccesoUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Primer acceso: la cuenta se creó con un código temporal y hay que dejar la clave propia
// y la pregunta de seguridad antes de usar la aplicación.
class PrimerAccesoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

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

    fun guardar(): PerfilUsuario? {
        val persona = persona ?: run {
            _aviso.tryEmit("No encontramos tu cuenta. Vuelve a entrar.")
            return null
        }
        val revisado = _uiState.value.validar()
        _uiState.value = revisado
        if (!revisado.puedeGuardar) return null

        val guardado = repositorio.definirClaveDefinitiva(
            idUsuario = persona.id,
            rut = persona.rut,
            clave = revisado.clave,
            idPregunta = revisado.idPregunta!!,
            respuesta = revisado.respuesta
        )
        if (!guardado) {
            _aviso.tryEmit("No pudimos guardar. Inténtalo otra vez.")
            return null
        }
        return persona.perfil
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
