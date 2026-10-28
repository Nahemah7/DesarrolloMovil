package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.EstadoRed
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.MedioConexion
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SinSenalViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val red = EstadoRed(application)

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
        _mensaje.value = if (_conectado.value) {
            "Sesión enviada. Queda por validar el supervisor."
        } else {
            "Sin conexión: la sesión sigue guardada en este teléfono."
        }
    }

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
