package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.IndicadoresUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Indicadores del dia. Todo sale de las vistas SQL: la conversion, el transito por
 * acceso y la cobertura de bloques. La app no recalcula nada por su cuenta.
 */
class IndicadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    // El dia que se mira es hoy: es una app de uso diario, no una foto del dia del anexo.
    // Si la tienda todavia no cargo los ingresos de hoy, se dice y se muestra lo que si hay.
    private val fecha = repositorio.fechaHoy()

    private val _uiState = MutableStateFlow(
        IndicadoresUiState(
            indicadores = repositorio.indicadoresDeFecha(fecha),
            porAcceso = repositorio.transitoPorAcceso(fecha),
            fecha = fecha,
            sesionesHoy = repositorio.sesionesDeFecha(fecha)
        )
    )
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    /** Se llama al volver a la pantalla: los indicadores cambian cuando se valida una sesion. */
    fun recargar() {
        _uiState.update {
            it.copy(
                indicadores = repositorio.indicadoresDeFecha(fecha),
                porAcceso = repositorio.transitoPorAcceso(fecha),
                sesionesHoy = repositorio.sesionesDeFecha(fecha)
            )
        }
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
