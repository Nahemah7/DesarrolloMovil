package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.model.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lo que el observador ve antes de empezar: quien es y que sesion le toca hoy.
 * Quien esta adentro sale de la sesion guardada, no de la pantalla.
 */
class MiSesionViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val _usuario = MutableStateFlow<Colaborador?>(null)
    val usuario: StateFlow<Colaborador?> = _usuario.asStateFlow()

    private val _sesionAsignada = MutableStateFlow<SesionResumen?>(null)
    val sesionAsignada: StateFlow<SesionResumen?> = _sesionAsignada.asStateFlow()

    /** El acceso de esa sesion, con su codigo y nombre para mostrar. */
    private val _acceso = MutableStateFlow("")
    val acceso: StateFlow<String> = _acceso.asStateFlow()

    private val _sinSesionProgramada = MutableStateFlow(false)
    val sinSesionProgramada: StateFlow<Boolean> = _sinSesionProgramada.asStateFlow()

    /** Todas las sesiones del observador en el dia, no solo una: puede tener varias programadas. */
    private val _sesionesDeHoy = MutableStateFlow<List<SesionResumen>>(emptyList())
    val sesionesDeHoy: StateFlow<List<SesionResumen>> = _sesionesDeHoy.asStateFlow()

    /** Los accesos, para mostrar de cual es cada sesion. */
    private val _accesos = MutableStateFlow<List<Acceso>>(emptyList())
    val accesos: StateFlow<List<Acceso>> = _accesos.asStateFlow()

    init {
        val id = sesion.leer()
        if (id > 0) {
            _usuario.value = repositorio.colaboradorPorId(id)
            val fecha = repositorio.fechaDeTrabajo()
            val propias = repositorio.sesionesDeFecha(fecha).filter { it.idObservador == id }
            val elegida = propias.firstOrNull { it.estado == EstadoSesion.PROGRAMADA }
                ?: propias.firstOrNull { it.estado == EstadoSesion.EN_CURSO }
            _sesionAsignada.value = elegida
            _sinSesionProgramada.value = elegida == null
            _sesionesDeHoy.value = propias
            _accesos.value = repositorio.accesos()
            // Si ya venia en curso, se muestra el acceso con el que se esta trabajando
            _acceso.value = repositorio.accesos()
                .firstOrNull { it.id == (elegida?.idAcceso ?: 0L) }?.etiquetaCorta ?: ""
        }
    }

    /** Deja en curso la sesion elegida de la lista y devuelve true para pasar al registro. */
    fun iniciar(idSesion: Long): Boolean {
        val elegida = _sesionesDeHoy.value.firstOrNull { it.id == idSesion } ?: return false
        val hora = ahora()
        if (elegida.estado == EstadoSesion.PROGRAMADA) repositorio.iniciarSesion(elegida.id, hora)
        _sesionesDeHoy.value = _sesionesDeHoy.value.map {
            if (it.id == elegida.id) it.copy(estado = EstadoSesion.EN_CURSO, inicioReal = hora) else it
        }
        _sesionAsignada.value = elegida.copy(estado = EstadoSesion.EN_CURSO, inicioReal = hora)
        return true
    }

    private fun ahora(): String =
        java.time.LocalTime.now().toString().let { if (it.length >= 8) it.take(8) else "$it:00" }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
