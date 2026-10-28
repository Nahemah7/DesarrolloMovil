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

class SesionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private var fechaElegida: String = repositorio.fechaHoy()

    private val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    private val formatoLargo = java.text.SimpleDateFormat("EEEE d 'de' MMMM", java.util.Locale("es", "CL"))

    private val idRevisor: Long = sesion.leer()

    private val _uiState = MutableStateFlow(SesionesUiState())
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    val accesos: List<Acceso> = repositorio.accesos()

    init {
        recargar()
    }

    fun recargar() {
        val pendientes = repositorio.bloquesPendientes(fechaElegida)
        val filtroActual = _uiState.value.filtro
        _uiState.value = SesionesUiState(
            sesiones = repositorio.sesionesDeFecha(fechaElegida),
            porValidar = repositorio.sesionesPorValidar(),
            deLaSemana = repositorio.sesionesDeLosUltimosDias(fechaElegida, 7),
            bloquesPendientes = pendientes.size,
            pendienteMasAtrasado = pendientes.firstOrNull() ?: "",
            filtro = filtroActual,
            fecha = fechaElegida,
            fechaLarga = fechaLarga(fechaElegida)
        )
    }

    fun diaAnterior() = moverDia(-1)

    fun diaSiguiente() = moverDia(1)

    fun irAHoy() {
        fechaElegida = repositorio.fechaHoy()
        _uiState.value = _uiState.value.copy(filtro = "Día")
        recargar()
    }

    fun elegirFecha(fecha: String) {
        fechaElegida = fecha
        _uiState.value = _uiState.value.copy(filtro = "Día")
        recargar()
    }

    private fun moverDia(dias: Int) {
        val calendario = java.util.Calendar.getInstance()
        calendario.time = formatoFecha.parse(fechaElegida) ?: java.util.Date()
        calendario.add(java.util.Calendar.DAY_OF_MONTH, dias)
        elegirFecha(formatoFecha.format(calendario.time))
    }

    private fun fechaLarga(fecha: String): String =
        formatoFecha.parse(fecha)?.let { formatoLargo.format(it) } ?: fecha

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
