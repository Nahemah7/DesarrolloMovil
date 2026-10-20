package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.IndicadoresUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Indicadores del dia. Todo sale de las vistas SQL: la conversion, el transito por
 * acceso y la cobertura de bloques. La app no recalcula nada por su cuenta.
 */
class IndicadoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val fecha = repositorio.fechaDeTrabajo()

    private val _uiState = MutableStateFlow(
        IndicadoresUiState(
            indicadores = repositorio.indicadoresDeFecha(fecha),
            porAcceso = repositorio.transitoPorAcceso(fecha)
        )
    )
    val uiState: StateFlow<IndicadoresUiState> = _uiState.asStateFlow()

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
