package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.EstadoRed
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.MedioConexion
import com.example.rinconsur.data.Sincronizador
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.TokenPreferencias
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SinSenalViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val red = EstadoRed(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)
    private val sincronizador = Sincronizador(repositorio, tokens, direccion)

    private val _conectado = MutableStateFlow(red.hayInternet())
    val conectado: StateFlow<Boolean> = _conectado.asStateFlow()

    private val _medio = MutableStateFlow(red.medioDeConexion())
    val medio: StateFlow<MedioConexion> = _medio.asStateFlow()

    private val _porEnviar = MutableStateFlow(porEnviar())
    val porEnviar: StateFlow<List<SesionResumen>> = _porEnviar.asStateFlow()

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun revisarRed() {
        _conectado.value = red.hayInternet()
        _medio.value = red.medioDeConexion()
    }

    fun enviar() {
        revisarRed()
        if (!_conectado.value) {
            _mensaje.value = "Sin conexión: la sesión sigue guardada en este teléfono."
            return
        }
        _mensaje.value = "Enviando los registros pendientes…"
        viewModelScope.launch {
            val resultado = sincronizador.enviarPendientes()
            _porEnviar.value = porEnviar()
            _mensaje.value = when {
                resultado.enviados > 0 && resultado.quedan == 0 ->
                    "Se enviaron " + resultado.enviados + " registros. Queda por validar el supervisor."
                resultado.enviados > 0 ->
                    "Se enviaron " + resultado.enviados + " y quedan " + resultado.quedan + " en el teléfono."
                resultado.quedan > 0 && resultado.motivo != null -> resultado.motivo
                resultado.quedan > 0 -> "No se pudo enviar: la sesión todavía no está en el servidor."
                else -> "No había registros pendientes."
            }
        }
    }

    fun pendientesDe(idSesion: Long): Int = repositorio.pendientesDeSesion(idSesion)

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    private fun porEnviar(): List<SesionResumen> =
        repositorio.sesionesDeFecha(repositorio.fechaHoy()).filter { it.estado == EstadoSesion.FINALIZADA }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
