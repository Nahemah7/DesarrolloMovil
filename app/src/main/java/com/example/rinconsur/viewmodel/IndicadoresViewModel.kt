package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.IndicadoresUiState
import com.example.rinconsur.model.SemanaIndicadores
import com.example.rinconsur.model.TipoPeriodo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class IndicadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    private val hoy = repositorio.fechaHoy()

    private val _uiState = MutableStateFlow(estadoDe(TipoPeriodo.HOY, 0))
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    fun onPeriodo(periodo: TipoPeriodo) {
        val posicion = if (periodo == TipoPeriodo.HOY) 0 else _uiState.value.posicionSemana
        _uiState.value = estadoDe(periodo, posicion)
    }

    fun onSemanaAnterior() {
        val s = _uiState.value
        if (!s.haySemanaAnterior) return
        _uiState.value = estadoDe(periodoDeSemana(s.periodo), s.posicionSemana + 1)
    }

    fun onSemanaSiguiente() {
        val s = _uiState.value
        if (!s.haySemanaSiguiente) return
        _uiState.value = estadoDe(periodoDeSemana(s.periodo), s.posicionSemana - 1)
    }

    fun recargar() {
        _uiState.value = estadoDe(_uiState.value.periodo, _uiState.value.posicionSemana)
    }

    private fun periodoDeSemana(periodo: TipoPeriodo): TipoPeriodo =
        if (periodo == TipoPeriodo.HOY) TipoPeriodo.HABILES else periodo

    private fun estadoDe(periodo: TipoPeriodo, posicion: Int): IndicadoresUiState {
        val semanas = repositorio.semanasConDatos().map { SemanaIndicadores(it.first, it.second) }
        val tipo = periodo.tipoDiaBd
        if (tipo == null) {
            return IndicadoresUiState(
                indicadores = repositorio.indicadoresDeFecha(hoy),
                porAcceso = repositorio.transitoPorAcceso(hoy),
                periodo = periodo,
                fecha = hoy,
                sesionesHoy = repositorio.sesionesDeFecha(hoy),
                hayDatosEnHabiles = repositorio.indicadoresDeTipoDia(TipoPeriodo.HABILES.tipoDiaBd!!) != null,
                semanas = semanas
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
            posicionSemana = donde
        )
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
