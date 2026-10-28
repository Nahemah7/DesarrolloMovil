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

/**
 * Indicadores de la tienda, con dos filtros: la semana y el tipo de dia.
 *
 * La semana se recorre con los botones de atras y adelante sobre las semanas que tienen datos, no
 * con un chip por semana: la lista va a crecer y una fila de fechas deja de caber. La posicion 0 es
 * "todas las semanas", que es el consolidado.
 *
 * Todo sale de las vistas SQL: la conversion, el transito por acceso y la cobertura de bloques. La
 * app no recalcula nada por su cuenta; para un periodo pide el consolidado a la base, que suma las
 * cifras y recalcula los porcentajes sobre el total en vez de promediar los dias.
 */
class IndicadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    // El dia que se mira es hoy: es una app de uso diario, no una foto del dia del anexo.
    // Si la tienda todavia no cargo los ingresos de hoy, se dice y se muestra lo que si hay.
    private val hoy = repositorio.fechaHoy()

    private val _uiState = MutableStateFlow(estadoDe(TipoPeriodo.HOY, 0))
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    /**
     * El filtro de tipo. "Hoy" no pertenece a ninguna semana cargada, asi que al elegirlo se suelta
     * la semana; al elegir una semana estando en "Hoy" se pasa a habiles, que es lo que tiene sentido.
     */
    fun onPeriodo(periodo: TipoPeriodo) {
        val posicion = if (periodo == TipoPeriodo.HOY) 0 else _uiState.value.posicionSemana
        _uiState.value = estadoDe(periodo, posicion)
    }

    /** Una semana hacia atras (mas vieja). */
    fun onSemanaAnterior() {
        val s = _uiState.value
        if (!s.haySemanaAnterior) return
        _uiState.value = estadoDe(periodoDeSemana(s.periodo), s.posicionSemana + 1)
    }

    /** Una semana hacia adelante (mas nueva), hasta "todas". */
    fun onSemanaSiguiente() {
        val s = _uiState.value
        if (!s.haySemanaSiguiente) return
        _uiState.value = estadoDe(periodoDeSemana(s.periodo), s.posicionSemana - 1)
    }

    /** Se llama al volver a la pantalla: los indicadores cambian cuando se valida una sesion. */
    fun recargar() {
        _uiState.value = estadoDe(_uiState.value.periodo, _uiState.value.posicionSemana)
    }

    /** Con una semana elegida no se puede mirar "hoy": se cae a habiles. */
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
