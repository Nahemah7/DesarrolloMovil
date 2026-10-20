package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.EntradaUiState
import com.example.rinconsur.model.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pantalla de sesion iniciada: quien esta adentro (sale de la sesion guardada, no de la pantalla)
 * y los accesos de la tienda, que se leen del Repository. Es el punto donde se ve que la cadena
 * completa funciona: login, base local y datos del caso.
 */
class EntradaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(EntradaUiState())
    val uiState: StateFlow<EntradaUiState> = _uiState.asStateFlow()

    init {
        val id = sesion.leer()
        val yo = if (id > 0) repositorio.colaboradorPorId(id) else null
        _uiState.value = EntradaUiState(
            nombre = yo?.nombre ?: "",
            rut = yo?.rut ?: "",
            perfil = yo?.perfil ?: PerfilUsuario.OBSERVADOR,
            accesos = repositorio.accesos()
        )
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
