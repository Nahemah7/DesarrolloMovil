package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.MotivoRechazo
import com.example.rinconsur.model.RechazarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Rechazo de una sesion: el supervisor elige los motivos en casillas y, si elige "Otro motivo",
 * tiene que escribir cual (la casilla de texto se activa sola). La validacion vive aca.
 *
 * El rechazo no borra nada: la sesion queda rechazada con quien la reviso, cuando y por que, y sus
 * registros se conservan para volver a contar ese bloque.
 */
class RechazarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val catalogoMotivos: List<MotivoRechazo> = MotivoRechazo.entries

    private val _uiState = MutableStateFlow(RechazarUiState())
    val uiState: StateFlow<RechazarUiState> = _uiState.asStateFlow()

    fun cargar(idSesion: Long) {
        if (idSesion <= 0L || _uiState.value.codigo.isNotEmpty()) return
        // Por id y sin importar la fecha: la sesion que se rechaza puede ser de dias anteriores
        val sesion = repositorio.sesion(idSesion) ?: return
        val acceso = repositorio.accesos().firstOrNull { it.id == sesion.idAcceso }
        _uiState.value = RechazarUiState(
            codigo = sesion.codigo,
            acceso = (acceso?.codigo ?: "") + " " + (acceso?.nombre ?: ""),
            observador = sesion.nombreObservador,
            hora = sesion.horaInicioCorta + " - " + sesion.horaFinCorta,
            registros = sesion.registrosValidos,
            pctTransito = sesion.pctTransito
        )
    }

    fun onMotivo(motivo: MotivoRechazo) {
        _uiState.update {
            val nuevos = if (it.motivos.contains(motivo)) it.motivos - motivo else it.motivos + motivo
            it.copy(
                motivos = nuevos,
                errorMotivos = null,
                // Si saca "Otro motivo", se borra el texto: no tiene sentido pedirlo
                otroMotivo = if (nuevos.contains(MotivoRechazo.OTRO)) it.otroMotivo else "",
                errorOtro = null
            )
        }
    }

    fun onOtroMotivo(texto: String) {
        _uiState.update { it.copy(otroMotivo = texto, errorOtro = null) }
    }

    /** Valida por campo y guarda el rechazo. */
    fun rechazar(idSesion: Long, idRevisor: Long): Boolean {
        val estado = _uiState.value
        val errorMotivos = if (estado.motivos.isEmpty()) "Elige al menos un motivo" else null
        val errorOtro = if (estado.exigeOtro && estado.otroMotivo.trim().length < 10)
            "Cuenta el motivo (10 caracteres minimo)" else null

        if (errorMotivos != null || errorOtro != null) {
            _uiState.update { it.copy(errorMotivos = errorMotivos, errorOtro = errorOtro) }
            return false
        }

        // El motivo se guarda como texto: las etiquetas elegidas, y el detalle si es "otro"
        val motivo = estado.motivos
            .map { if (it == MotivoRechazo.OTRO) it.etiqueta + ": " + estado.otroMotivo.trim() else it.etiqueta }
            .joinToString("; ")

        repositorio.revisarSesion(idSesion, EstadoSesion.RECHAZADA, idRevisor, motivo)
        _uiState.update { it.copy(rechazada = true) }
        return true
    }

    val motivos: List<MotivoRechazo> get() = catalogoMotivos

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
