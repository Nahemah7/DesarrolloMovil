package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioIndicadoresRemoto
import com.example.rinconsur.data.remoto.ReporteRemoto
import com.example.rinconsur.data.remoto.aReporteJefatura
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.ConversionMes
import com.example.rinconsur.model.FuenteDatos
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.model.HoraPunta
import com.example.rinconsur.model.MetaDelMes
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.PersonalRendimiento
import com.example.rinconsur.model.ReporteJefatura
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ReporteViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private val idUsuario: Long = sesion.leer()

    private val _reporte = MutableStateFlow<ReporteJefatura?>(null)
    val reporte: StateFlow<ReporteJefatura?> = _reporte.asStateFlow()

    private val _personal = MutableStateFlow<List<PersonalRendimiento>>(emptyList())
    val personal: StateFlow<List<PersonalRendimiento>> = _personal.asStateFlow()

    private val _fuente = MutableStateFlow(FuenteDatos.LOCAL)
    val fuente: StateFlow<FuenteDatos> = _fuente.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    val puedeRegistrarMeta: Boolean =
        repositorio.colaboradorPorId(idUsuario)?.perfil == PerfilUsuario.JEFE_TIENDA

    fun cargar() {
        _reporte.value = repositorio.reporteJefatura()
        _personal.value = repositorio.rendimientoDelPersonal()
        cargarDelServicio()
    }

    private fun cargarDelServicio() {
        val token = tokens.leer() ?: return
        TrabajoDeFondo.alcance.launch {
            val fecha = repositorio.fechaHoy()
            val cliente = RepositorioIndicadoresRemoto(direccion.leer(), token)
            cliente.personal()?.let { _personal.value = it }
            val remoto = cliente.reporte(fecha) ?: return@launch
            _reporte.value = remoto.aReporteJefatura(repositorio.bloques())
            _fuente.value = FuenteDatos.EN_LINEA
        }
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
