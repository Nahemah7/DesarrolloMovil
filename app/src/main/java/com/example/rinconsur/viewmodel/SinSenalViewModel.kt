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

/**
 * Cola de sesiones por enviar. El telefono guarda siempre; el envio depende de la red,
 * y el medio de conexion se lee del sistema (recurso nativo), no se deduce.
 */
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

    /** Vuelve a preguntarle al sistema: es lo que hace el boton "Reintentar". */
    fun revisarRed() {
        _conectado.value = red.hayInternet()
        _medio.value = red.medioDeConexion()
    }

    fun enviar() {
        revisarRed()
        _mensaje.value = if (_conectado.value) {
            "Sesion enviada. Queda por validar el supervisor."
        } else {
            "Sin conexion: la sesion sigue guardada en este telefono."
        }
    }

    fun limpiarMensaje() {
        _mensaje.value = null
    }

    /** Las sesiones que ya se cerraron y todavia no estan validadas: lo que falta enviar. */
    private fun porEnviar(): List<SesionResumen> =
        repositorio.sesionesDeFecha(repositorio.fechaDeTrabajo()).filter { it.estado == EstadoSesion.FINALIZADA }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
