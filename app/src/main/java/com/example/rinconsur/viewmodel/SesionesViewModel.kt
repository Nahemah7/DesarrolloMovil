package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Las sesiones del dia para el supervisor: validar, rechazar y ver que quedo pendiente. */
class SesionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val fecha = repositorio.fechaDeTrabajo()

    /** Quien revisa sale de la sesion guardada: el que valida nunca es el observador. */
    private val idRevisor: Long = sesion.leer()

    private val _uiState = MutableStateFlow(SesionesUiState())
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    /** Catalogo de accesos: la pantalla lo usa para escribir "A4 Conexion" y no "4". */
    val accesos: List<Acceso> = repositorio.accesos()

    init {
        recargar()
    }

    private fun recargar() {
        val pendientes = repositorio.bloquesPendientes()
        _uiState.value = SesionesUiState(
            sesiones = repositorio.sesionesDeFecha(fecha),
            // Las que esperan revision pueden ser de dias anteriores: el supervisor las tiene que ver
            porValidar = repositorio.sesionesPorValidar(),
            deLaSemana = repositorio.sesionesDeLosUltimosDias(fecha, 7),
            bloquesPendientes = pendientes.size,
            pendienteMasAtrasado = pendientes.firstOrNull() ?: ""
        )
    }

    fun filtrar(filtro: String) {
        _uiState.value = _uiState.value.copy(filtro = filtro)
    }

    fun validar(idSesion: Long) {
        repositorio.revisarSesion(idSesion, EstadoSesion.VALIDADA, idRevisor)
        recargar()
    }

    fun rechazar(idSesion: Long, motivo: String) {
        repositorio.revisarSesion(idSesion, EstadoSesion.RECHAZADA, idRevisor, motivo)
        recargar()
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
