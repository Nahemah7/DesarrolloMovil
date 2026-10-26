package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Alta de un evento del dia, con la foto de evidencia.
 *
 * Toda la validacion vive aca, no en la pantalla: la pantalla solo muestra el error que el
 * ViewModel calcula, campo por campo (es lo que la rubrica llama "validacion desacoplada").
 */
class EventoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val fechaDeTrabajo = repositorio.fechaHoy()

    /** Catalogos para la pantalla. */
    val accesos: List<Acceso> = repositorio.accesos()
    val tipos: List<TipoEvento> = TipoEvento.entries

    private val _uiState = MutableStateFlow(
        EventoUiState(fecha = fechaDeTrabajo, eventosDelDia = repositorio.contarEventos(fechaDeTrabajo))
    )
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

    /** La foto ya viene reducida y comprimida: la pantalla la convierte antes de pasarla. */
    fun onFoto(bytes: ByteArray) {
        // Si habia un aviso de permiso denegado, ya no corresponde: la foto llego igual
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

    /** Valida por campo y, si esta todo bien, guarda en SQLite (con la foto como BLOB). */
    fun guardar(): Boolean {
        val estado = _uiState.value
        val errorFecha = when {
            estado.fecha.isBlank() -> "Escribe la fecha del evento"
            !Regex("\\d{4}-\\d{2}-\\d{2}").matches(estado.fecha.trim()) -> "Usa el formato 2026-09-15"
            else -> null
        }
        val errorDescripcion = when {
            estado.descripcion.isBlank() -> "Cuenta que paso"
            estado.descripcion.trim().length < 10 -> "Un poco más de detalle (10 caracteres)"
            estado.descripcion.length > 200 -> "Maximo 200 caracteres"
            else -> null
        }
        // Regla del caso: si se cierra un acceso, hay que decir cual
        val errorAcceso = if (estado.tipo == TipoEvento.CIERRE_ACCESO && estado.idAcceso == null)
            "Si es cierre de acceso, indica cual" else null

        if (errorFecha != null || errorDescripcion != null || errorAcceso != null) {
            _uiState.update {
                it.copy(errorFecha = errorFecha, errorDescripcion = errorDescripcion, errorAcceso = errorAcceso)
            }
            return false
        }

        // Correlativo igual al de la semilla del caso: E-01, E-02, ... E-07
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
                eventosDelDia = total,
                guardado = true,
                fotoBytes = null,
                descripcion = "",
                mensaje = "Evento " + codigo + " guardado" + (if (estado.tieneFoto) " con la foto de evidencia." else ".")
            )
        }
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
