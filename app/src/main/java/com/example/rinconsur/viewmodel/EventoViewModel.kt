package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.mensajeDeTextoLibre
import com.example.rinconsur.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val fechaDeTrabajo = repositorio.fechaHoy()

    val accesos: List<Acceso> = repositorio.accesos()
    val tipos: List<TipoEvento> = TipoEvento.entries

    private val _uiState = MutableStateFlow(
        EventoUiState(
            fecha = fechaDeTrabajo,
            eventosDelDia = repositorio.contarEventos(fechaDeTrabajo),
            eventos = repositorio.eventosRegistrados()
        )
    )

    private fun recargarEventos() {
        _uiState.update {
            it.copy(
                eventos = repositorio.eventosRegistrados(),
                eventosDelDia = repositorio.contarEventos(fechaDeTrabajo)
            )
        }
    }
    val uiState: StateFlow<EventoUiState> = _uiState.asStateFlow()

    fun onFecha(valor: String) {
        _uiState.update { it.copy(fecha = valor, errorFecha = null, guardado = false) }
    }

    fun onTipo(tipo: TipoEvento) {
        _uiState.update {
            it.copy(tipo = tipo, errorAcceso = null, guardado = false)
        }
    }

    fun onAcceso(idAcceso: Long?) {
        _uiState.update { it.copy(idAcceso = idAcceso, errorAcceso = null, guardado = false) }
    }

    fun onDescripcion(valor: String) {
        _uiState.update { it.copy(descripcion = valor, errorDescripcion = null, guardado = false) }
    }

    fun onFoto(bytes: ByteArray) {
        _uiState.update { it.copy(fotoBytes = bytes, guardado = false, mensaje = null) }
    }

    fun quitarFoto() {
        _uiState.update { it.copy(fotoBytes = null, mensaje = null) }
    }

    fun onPermisoDenegado() {
        _uiState.update {
            it.copy(mensaje = "Sin permiso de camara. Puedes adjuntar la foto desde la galeria.")
        }
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun guardar(): Boolean {
        val estado = _uiState.value

        val errorFecha = when {
            estado.fecha.isBlank() -> "Escribe la fecha del evento"
            !Regex("\\d{4}-\\d{2}-\\d{2}").matches(estado.fecha.trim()) -> "Usa el formato 2026-09-15"
            else -> null
        }
        val errorDescripcion = mensajeDeTextoLibre(estado.descripcion, "Cuenta qué pasó")

        val errorAcceso = if (estado.tipo == TipoEvento.CIERRE_ACCESO && estado.idAcceso == null)
            "Si es cierre de acceso, indica cual" else null

        if (errorFecha != null || errorDescripcion != null || errorAcceso != null) {
            _uiState.update {
                it.copy(errorFecha = errorFecha, errorDescripcion = errorDescripcion, errorAcceso = errorAcceso)
            }
            return false
        }

        val codigo = "E-" + String.format("%02d", repositorio.contarTodosLosEventos() + 1)
        repositorio.registrarEvento(
            EventoDia(
                codigo = codigo,
                fechaInicio = estado.fecha.trim(),
                fechaFin = estado.fecha.trim(),
                tipo = estado.tipo,
                idAcceso = estado.idAcceso,
                descripcion = estado.descripcion.trim(),
                idUsuarioRegistra = sesion.leer().coerceAtLeast(1L),
                foto = estado.fotoBytes
            )
        )
        val total = repositorio.contarEventos(estado.fecha)
        _uiState.update {
            it.copy(
                eventos = repositorio.eventosRegistrados(),
                eventosDelDia = total,
                guardado = true,
                fotoBytes = null,
                descripcion = "",
                mensaje = "Evento " + codigo + " guardado" + (if (estado.tieneFoto) " con la foto de evidencia." else ".")
            )
        }
        return true
    }

    fun anular(idEvento: Long, motivo: String): String? {
        val error = mensajeDeTextoLibre(motivo, "Escribe el motivo de la anulación")
        if (error != null) return error

        repositorio.anularEvento(idEvento, motivo.trim(), sesion.leer().coerceAtLeast(1L))
        recargarEventos()
        _uiState.update { it.copy(mensaje = "Evento anulado. Queda en la lista con su motivo.") }
        return null
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
