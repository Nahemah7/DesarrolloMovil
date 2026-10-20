package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.model.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Lo que el observador ve antes de empezar: quien es y que sesion le toca hoy.
 * Quien esta adentro sale de la sesion guardada, igual que en el material del ramo.
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
            // Si ya venia en curso, se muestra el acceso con el que se esta trabajando
            _acceso.value = repositorio.accesos()
                .firstOrNull { it.id == (elegida?.idAcceso ?: 0L) }?.etiquetaCorta ?: ""
        }
    }

    /** Deja la sesion En curso y devuelve su id para pasar al registro. */
    fun iniciar(): Long? {
        val actual = _sesionAsignada.value ?: return null
        if (actual.estado == EstadoSesion.PROGRAMADA) {
            repositorio.iniciarSesion(actual.id, ahora())
            _sesionAsignada.value = actual.copy(
                estado = EstadoSesion.EN_CURSO,
                inicioReal = ahora()
            )
        }
        return actual.id
    }

    private fun ahora(): String =
        java.time.LocalTime.now().toString().let { if (it.length >= 8) it.take(8) else "$it:00" }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
