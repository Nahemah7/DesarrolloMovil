package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.ResumenSesion
import com.example.rinconsur.model.SesionResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cierre de la sesion: el resumen de lo registrado y el envio a validacion. */
class CierreViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val idObservador = sesion.leer()
    private val sesionActual: SesionResumen? =
        repositorio.sesionEnCursoDe(idObservador) ?: repositorio.sesionesDeFecha(repositorio.fechaDeTrabajo()).firstOrNull { it.idObservador == idObservador }

    private val _resumen = MutableStateFlow(repositorio.resumenDeSesion(sesionActual?.id ?: 0L))
    val resumen: StateFlow<ResumenSesion> = _resumen.asStateFlow()

    private val _codigo = MutableStateFlow(sesionActual?.codigo ?: "")
    val codigo: StateFlow<String> = _codigo.asStateFlow()

    val sePuedeValidar: Boolean get() = _resumen.value.total >= FlujoRepository.MINIMO_REGISTROS

    fun cerrar(): Boolean {
        if (!sePuedeValidar) return false
        val id = sesionActual?.id ?: return false
        repositorio.cerrarSesion(id, java.time.LocalTime.now().toString().take(8))
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
