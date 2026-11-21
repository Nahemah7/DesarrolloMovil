package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.IndicadoresUiState
import com.example.rinconsur.model.SemanaIndicadores
import com.example.rinconsur.model.TipoPeriodo
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioIndicadoresRemoto
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import com.example.rinconsur.model.FuenteDatos
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IndicadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private val hoy = repositorio.fechaHoy()

    private val _uiState = MutableStateFlow(estadoDe(TipoPeriodo.HOY, 0))
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    init {
        sincronizar()
    }

    fun onPeriodo(periodo: TipoPeriodo) {
        val posicion = if (periodo == TipoPeriodo.HOY) 0 else _uiState.value.posicionSemana
        _uiState.value = estadoDe(periodo, posicion)
        sincronizar()
    }

    // Las cifras se piden al servicio y, si contesta, reemplazan a las del teléfono. El desglose
    // por acceso se queda local: el servicio todavía no lo publica, y los dos salen de las mismas
    // vistas, así que no pueden discrepar.
    private fun sincronizar() {
        val token = tokens.leer() ?: return
        TrabajoDeFondo.alcance.launch {
            val s = _uiState.value
            val remoto = RepositorioIndicadoresRemoto(direccion.leer(), token)
            val tipo = s.periodo.tipoDiaBd
            val cifras = if (tipo == null) {
                remoto.delDia(s.fecha)
            } else {
                val semana = s.semanaElegida
                remoto.delPeriodo(tipo, semana?.desde, semana?.hasta)
            }
            if (cifras != null) {
                _uiState.value = _uiState.value.copy(indicadores = cifras, fuente = FuenteDatos.EN_LINEA)
            }
        }
    }

    fun onSemanaAnterior() {
        val s = _uiState.value
        if (!s.haySemanaAnterior) return
        _uiState.value = estadoDe(periodoDeSemana(s.periodo), s.posicionSemana + 1)
        sincronizar()
    }

    fun onSemanaSiguiente() {
        val s = _uiState.value
        if (!s.haySemanaSiguiente) return
        _uiState.value = estadoDe(periodoDeSemana(s.periodo), s.posicionSemana - 1)
        sincronizar()
    }

    fun recargar() {
        _uiState.value = estadoDe(_uiState.value.periodo, _uiState.value.posicionSemana)
        sincronizar()
    }

    private fun periodoDeSemana(periodo: TipoPeriodo): TipoPeriodo =
        if (periodo == TipoPeriodo.HOY) TipoPeriodo.HABILES else periodo

    private fun estadoDe(periodo: TipoPeriodo, posicion: Int): IndicadoresUiState {
        val semanas = repositorio.semanasConDatos().map { SemanaIndicadores(it.first, it.second) }
        val bloquesDelDia = repositorio.bloques().size
        val tipo = periodo.tipoDiaBd
        if (tipo == null) {
            return IndicadoresUiState(
                indicadores = repositorio.indicadoresDeFecha(hoy),
                porAcceso = repositorio.transitoPorAcceso(hoy),
                periodo = periodo,
                fecha = hoy,
                sesionesHoy = repositorio.sesionesDeFecha(hoy),
                hayDatosEnHabiles = repositorio.indicadoresDeTipoDia(TipoPeriodo.HABILES.tipoDiaBd!!) != null,
                semanas = semanas,
                deEjemplo = repositorio.datosDeEjemplo(),
                medicion = repositorio.medicionDeFecha(hoy),
                bloquesDelDia = bloquesDelDia
            )
        }
        val donde = posicion.coerceIn(0, semanas.size)
        val semana = semanas.getOrNull(donde - 1)
        return IndicadoresUiState(
            indicadores = repositorio.indicadoresDeTipoDia(tipo, semana?.desde, semana?.hasta),
            porAcceso = repositorio.transitoPorAccesoDeTipo(tipo, semana?.desde, semana?.hasta),
            periodo = periodo,
            tipoDia = tipo,
            fecha = hoy,
            sesionesHoy = repositorio.sesionesDeFecha(hoy),
            semanas = semanas,
            posicionSemana = donde,
            deEjemplo = repositorio.datosDeEjemplo(),
            medicion = repositorio.medicionDeTipo(tipo, semana?.desde, semana?.hasta),
            bloquesDelDia = bloquesDelDia
        )
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
