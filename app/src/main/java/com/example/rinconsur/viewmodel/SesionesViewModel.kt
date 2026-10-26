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

    /** El dia que se esta revisando. Arranca en el dia de trabajo y se mueve con las flechas. */
    private var fechaElegida: String = repositorio.fechaHoy()

    private val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    private val formatoLargo = java.text.SimpleDateFormat("EEEE d 'de' MMMM", java.util.Locale("es", "CL"))

    /** Quien revisa sale de la sesion guardada: el que valida nunca es el observador. */
    private val idRevisor: Long = sesion.leer()

    private val _uiState = MutableStateFlow(SesionesUiState())
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    /** Catalogo de accesos: la pantalla lo usa para escribir "A4 Conexión" y no "4". */
    val accesos: List<Acceso> = repositorio.accesos()

    init {
        recargar()
    }

    /**
     * Relee la lista de la base. Es publica porque la pantalla la vuelve a llamar al volver del
     * formulario de rechazo: la validacion y el motivo se guardan desde otra pantalla, y sin esto la
     * lista se quedaba con el estado viejo.
     */
    fun recargar() {
        // Los bloques sin medir se piden para el dia que se esta mirando.
        val pendientes = repositorio.bloquesPendientes(fechaElegida)
        // El filtro elegido se conserva: al volver del rechazo la lista se relee y el supervisor
        // tiene que seguir viendo la misma pestaña, no volver a "Hoy".
        val filtroActual = _uiState.value.filtro
        _uiState.value = SesionesUiState(
            sesiones = repositorio.sesionesDeFecha(fechaElegida),
            // Las que esperan revision pueden ser de dias anteriores: el supervisor las tiene que ver
            porValidar = repositorio.sesionesPorValidar(),
            deLaSemana = repositorio.sesionesDeLosUltimosDias(fechaElegida, 7),
            bloquesPendientes = pendientes.size,
            pendienteMasAtrasado = pendientes.firstOrNull() ?: "",
            filtro = filtroActual,
            fecha = fechaElegida,
            fechaLarga = fechaLarga(fechaElegida)
        )
    }

    /** Un dia hacia atras. */
    fun diaAnterior() = moverDia(-1)

    /** Un dia hacia adelante. */
    fun diaSiguiente() = moverDia(1)

    /** Vuelve al dia de trabajo. */
    fun irAHoy() {
        fechaElegida = repositorio.fechaHoy()
        _uiState.value = _uiState.value.copy(filtro = "Día")
        recargar()
    }

    /** El dia elegido en el calendario, en yyyy-MM-dd. */
    fun elegirFecha(fecha: String) {
        fechaElegida = fecha
        // Al cambiar de dia se vuelve a la vista del dia: el filtro de pendientes es de todas las fechas.
        _uiState.value = _uiState.value.copy(filtro = "Día")
        recargar()
    }

    private fun moverDia(dias: Int) {
        val calendario = java.util.Calendar.getInstance()
        calendario.time = formatoFecha.parse(fechaElegida) ?: java.util.Date()
        calendario.add(java.util.Calendar.DAY_OF_MONTH, dias)
        elegirFecha(formatoFecha.format(calendario.time))
    }

    /** "2026-09-15" -> "martes 15 de septiembre". */
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
