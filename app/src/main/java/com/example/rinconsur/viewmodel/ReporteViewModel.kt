package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.ReporteJefatura
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val idUsuario: Long = sesion.leer()

    private val _reporte = MutableStateFlow<ReporteJefatura?>(null)
    val reporte: StateFlow<ReporteJefatura?> = _reporte.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    val puedeRegistrarMeta: Boolean =
        repositorio.colaboradorPorId(idUsuario)?.perfil == PerfilUsuario.JEFE_TIENDA

    fun cargar() {
        _reporte.value = repositorio.reporteJefatura()
    }

    fun registrarMeta(texto: String): Boolean {
        val valor = texto.trim().replace(',', '.').toDoubleOrNull()
        if (valor == null || valor < 0 || valor > 100) {
            _aviso.tryEmit("La meta es un porcentaje entre 0 y 100.")
            return false
        }
        val actual = _reporte.value?.meta
        if (actual == null || actual.anio == 0) {
            _aviso.tryEmit("No se pudo registrar la meta: falta el período.")
            return false
        }
        val guardado = repositorio.guardarMeta(actual.anio, actual.mes, valor, idUsuario)
        cargar()
        _aviso.tryEmit(if (guardado) "Meta del mes registrada." else "No se pudo guardar la meta.")
        return guardado
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
