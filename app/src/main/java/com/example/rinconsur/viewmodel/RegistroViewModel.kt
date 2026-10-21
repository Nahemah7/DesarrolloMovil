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
 * El registro en si: la parte que se usa de pie, con una mano y sin escribir (RNF-01, RNF-02).
 * Se guarda con un toque y el resumen se vuelve a leer de la base (nada de contadores en memoria).
 */
class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val idObservador: Long = sesion.leer()
    private val idSesion: Long = repositorio.sesionEnCursoDe(idObservador)?.id
        ?: repositorio.sesionesDeFecha(repositorio.fechaDeTrabajo()).firstOrNull { it.idObservador == idObservador }?.id
        ?: 0L

    private val _uiState = MutableStateFlow(
        RegistroUiState(
            codigoSesion = repositorio.resumenDeSesion(idSesion).codigo,
            resumen = repositorio.resumenDeSesion(idSesion),
            ultimosRegistros = repositorio.ultimosRegistros(idSesion),
            accesos = repositorio.accesos(),
            sesionEnCurso = repositorio.sesionEnCursoDe(idObservador)?.id == idSesion
        )
    )
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

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

    /** Guardar y seguir: no corta el ritmo de la observacion. */
    fun guardar(): Boolean {
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
        repositorio.registrar(
            idSesion = idSesion,
            clasificacion = estado.clasificacion,
            rangoEtario = rangoEtario,
            compania = estado.compania,
            comprasOtraTienda = estado.comprasOtraTienda,
            seDirige = estado.destino
        )
        refrescar(mensaje = null)
        return true
    }

    /** Anular es lo excepcional: deja rastro, no borra (RN-11). */
    fun anular(idRegistro: Long, motivo: String) {
        repositorio.anularRegistro(idRegistro, motivo, idObservador)
        refrescar(mensaje = "Registro anulado")
    }

    fun limpiarMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    private fun refrescar(mensaje: String?) {
        _uiState.update {
            it.copy(
                resumen = repositorio.resumenDeSesion(idSesion),
                ultimosRegistros = repositorio.ultimosRegistros(idSesion),
                mensaje = mensaje
            )
        }
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
