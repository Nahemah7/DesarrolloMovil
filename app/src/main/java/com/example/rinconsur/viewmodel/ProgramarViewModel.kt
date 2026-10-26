package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.ProgramarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Programar una sesion de conteo. Quien programa es el colaborador que tiene la sesion abierta,
 * no un dato de la pantalla.
 */
class ProgramarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val idProgramador: Long = sesion.leer()

    private val _uiState = MutableStateFlow(ProgramarUiState())
    val uiState: StateFlow<ProgramarUiState> = _uiState.asStateFlow()

    init {
        // La sesion se programa para hoy: el conteo se hace el dia que se hace.
        val fecha = repositorio.fechaHoy()
        _uiState.update {
            it.copy(
                fecha = fecha,
                accesos = repositorio.accesos(),
                observadores = repositorio.observadores(),
                sesionesDeLaFecha = repositorio.sesionesDeFecha(fecha)
            )
        }
    }

    fun onAcceso(id: Long) = _uiState.update { it.copy(idAcceso = id, error = null, programada = "", codigo = "") }

    fun onObservador(id: Long) = _uiState.update { it.copy(idObservador = id, error = null, programada = "", codigo = "") }

    fun onHoraInicio(delta: Int) = _uiState.update {
        it.copy(horaInicio = mover(it.horaInicio, delta), error = null, programada = "", codigo = "")
    }

    fun onHoraFin(delta: Int) = _uiState.update {
        it.copy(horaFin = mover(it.horaFin, delta), error = null, programada = "", codigo = "")
    }

    /** Devuelve true si quedo programada. Si no, deja el motivo en el estado. */
    fun programar(): Boolean {
        val e = _uiState.value
        val problema = when {
            e.idAcceso == null -> "Elige el acceso donde se va a contar."
            e.idObservador == null -> "Elige quién va a contar."
            !e.duracionValida -> "El horario debe durar entre 30 y 60 minutos."
            e.choque != null -> textoDeChoque(e.choque!!, e)
            else -> null
        }
        if (problema != null) {
            _uiState.update { it.copy(error = problema) }
            return false
        }
        val idAcceso = e.idAcceso!!
        val idObservador = e.idObservador!!
        val codigo = repositorio.programarSesion(idAcceso, e.fecha, e.horaInicio, e.horaFin, idObservador, idProgramador)
        _uiState.update {
            it.copy(
                error = null,
                codigo = codigo,
                programada = nombreDe(e, idObservador),
                sesionesDeLaFecha = repositorio.sesionesDeFecha(e.fecha),
                // El formulario queda limpio para la proxima: si no, la sesion recien creada
                // aparece como choque de si misma y sigue mostrando el aviso de que se cruza.
                idAcceso = null,
                idObservador = null,
                horaInicio = "13:00",
                horaFin = "13:45"
            )
        }
        return true
    }

    private fun nombreDe(e: ProgramarUiState, idObservador: Long): String =
        e.observadores.firstOrNull { it.id == idObservador }?.nombre ?: "el observador"

    private fun textoDeChoque(choque: com.example.rinconsur.model.SesionResumen, e: ProgramarUiState): String {
        val donde = if (choque.idAcceso == e.idAcceso) {
            e.accesos.firstOrNull { it.id == choque.idAcceso }?.nombre ?: "Ese acceso"
        } else {
            e.observadores.firstOrNull { it.id == choque.idObservador }?.nombre ?: "Esa persona"
        }
        return donde + " ya tiene una sesión de " + choque.horaInicioCorta + " a " + choque.horaFinCorta + ": se cruza."
    }

    /** Mueve la hora en pasos de 5 minutos, dentro del dia. */
    private fun mover(hora: String, delta: Int): String {
        val partes = hora.split(":")
        val actual = (partes.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (partes.getOrNull(1)?.toIntOrNull() ?: 0)
        val nueva = (actual + delta).coerceIn(0, 23 * 60 + 55)
        return "%02d:%02d".format(nueva / 60, nueva % 60)
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
