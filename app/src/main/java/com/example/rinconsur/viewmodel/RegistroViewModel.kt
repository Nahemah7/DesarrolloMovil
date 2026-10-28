package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.Plantilla
import com.example.rinconsur.data.Plantillas
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val idObservador: Long = sesion.leer()

    private var idSesion: Long = repositorio.sesionEnCursoDe(idObservador)?.id ?: 0L

    private val _uiState = MutableStateFlow(
        RegistroUiState(
            codigoSesion = repositorio.resumenDeSesion(idSesion).codigo,
            resumen = repositorio.resumenDeSesion(idSesion),
            ultimosRegistros = repositorio.ultimosRegistros(idSesion),
            sinSesionEnCurso = idSesion == 0L,
            accesos = repositorio.accesos(),
            sesionEnCurso = repositorio.sesionEnCursoDe(idObservador)?.id == idSesion
        )
    )
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()


    fun releer() {
        idSesion = repositorio.sesionEnCursoDe(idObservador)?.id ?: 0L
        _uiState.update {
            it.copy(
                codigoSesion = repositorio.resumenDeSesion(idSesion).codigo,
                resumen = repositorio.resumenDeSesion(idSesion),
                ultimosRegistros = repositorio.ultimosRegistros(idSesion),
                sinSesionEnCurso = idSesion == 0L,
                sesionEnCurso = idSesion != 0L
            )
        }
    }

    fun onClasificacion(clasificacion: Clasificacion) {
        _uiState.update {
            it.copy(
                clasificacion = clasificacion,
                destino = if (clasificacion == Clasificacion.TRANSITO) it.destino else null
            )
        }
    }

    fun onRangoEtario(rangoEtario: RangoEtario) {
        _uiState.update { it.copy(rangoEtario = rangoEtario, mensaje = null) }
    }

    fun onCompania(compania: Compania) {
        _uiState.update { it.copy(compania = compania) }
    }

    fun onCompras(valor: Boolean) {
        _uiState.update { it.copy(comprasOtraTienda = valor) }
    }

    fun onDestino(idAcceso: Long) {
        _uiState.update { it.copy(destino = idAcceso) }
    }

    fun guardar(): Boolean {
        if (idSesion == 0L) {
            _uiState.update { it.copy(mensaje = "No tienes una sesión de conteo en curso.") }
            return false
        }
        val estado = _uiState.value
        val rangoEtario = estado.rangoEtario
        if (rangoEtario == null) {
            _uiState.update { it.copy(mensaje = "Elige la rangoEtario antes de guardar") }
            return false
        }
        if (estado.clasificacion == Clasificacion.TRANSITO && estado.destino == null) {
            _uiState.update { it.copy(mensaje = "Si es transito, indica hacia donde se dirige") }
            return false
        }
        val fila = repositorio.registrar(
            idSesion = idSesion,
            clasificacion = estado.clasificacion,
            rangoEtario = rangoEtario,
            compania = estado.compania,
            comprasOtraTienda = estado.comprasOtraTienda,
            seDirige = estado.destino
        )
        if (fila < 0) {
            refrescar(mensaje = "La sesión ya fue enviada a validación: no se pueden agregar registros.")
            return false
        }
        refrescar(mensaje = null)
        return true
    }

    private val guardadas = Plantillas(application)

    private val _plantillas = MutableStateFlow<List<Plantilla?>>(listOf(null, null, null))
    val plantillas: StateFlow<List<Plantilla?>> = _plantillas.asStateFlow()

    fun recargarPlantillas() {
        _plantillas.value = (1..3).map { guardadas.leer(it) }
    }

    fun aplicarPlantilla(numero: Int): Boolean {
        val plantilla = _plantillas.value.getOrNull(numero - 1) ?: return false
        _uiState.update {
            it.copy(
                clasificacion = plantilla.clasificacion,
                rangoEtario = plantilla.rangoEtario,
                compania = plantilla.compania,
                comprasOtraTienda = plantilla.comprasOtraTienda,
                destino = plantilla.destino,
                mensaje = "Plantilla " + numero + ": " + plantilla.clasificacion.etiqueta + " · " + plantilla.rangoEtario.etiqueta
            )
        }
        return true
    }

    fun anular(idRegistro: Long, motivo: String) {
        repositorio.anularRegistro(idRegistro, motivo, idObservador)
        refrescar(mensaje = "Registro anulado")
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun refrescar(mensaje: String?) {
        idSesion = repositorio.sesionEnCursoDe(idObservador)?.id ?: 0L
        _uiState.update {
            it.copy(
                codigoSesion = repositorio.resumenDeSesion(idSesion).codigo,
                resumen = repositorio.resumenDeSesion(idSesion),
                ultimosRegistros = repositorio.ultimosRegistros(idSesion),
                sinSesionEnCurso = idSesion == 0L,
                sesionEnCurso = idSesion != 0L,
                mensaje = mensaje
            )
        }
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
