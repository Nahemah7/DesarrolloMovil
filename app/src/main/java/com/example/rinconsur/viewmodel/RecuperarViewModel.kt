package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Clave
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.Rut
import com.example.rinconsur.data.mensajeDeClave
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PasoRecuperar
import com.example.rinconsur.model.RecuperarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RecuperarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    private val _uiState = MutableStateFlow(RecuperarUiState())
    val uiState: StateFlow<RecuperarUiState> = _uiState.asStateFlow()

    private var persona: Colaborador? = null

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, errorGeneral = null) }
    }

    fun buscar(): Boolean {
        val escrito = _uiState.value.rut
        if (Rut.validar(escrito) != null) {
            _uiState.update {
                it.copy(errorRut = "El RUT no es válido. Revísalo y escríbelo de nuevo.", errorGeneral = null)
            }
            return false
        }
        val rut = Rut.formatear(escrito)
        val encontrado = repositorio.colaboradorPorRut(rut)
        val cantidad = encontrado?.let { repositorio.preguntasDe(it.id).size } ?: 0
        if (encontrado == null || cantidad == 0) {
            _uiState.update {
                it.copy(
                    errorGeneral = "No pudimos validar tus datos. Revisa el RUT o consulta con tu supervisor.",
                    errorRut = null
                )
            }
            return false
        }
        persona = encontrado
        _uiState.update {
            it.copy(
                paso = PasoRecuperar.PREGUNTAS,
                catalogo = repositorio.preguntas(),
                elegidas = List(cantidad) { -1 },
                respuestas = List(cantidad) { "" },
                errorGeneral = null
            )
        }
        return true
    }

    fun onPreguntaElegida(indice: Int, posicion: Int) {
        _uiState.update {
            val nuevas = it.elegidas.toMutableList()
            if (indice in nuevas.indices) nuevas[indice] = posicion
            it.copy(elegidas = nuevas, errorRespuestas = null, errorGeneral = null)
        }
    }

    fun onRespuestaCambia(indice: Int, valor: String) {
        _uiState.update {
            val nuevas = it.respuestas.toMutableList()
            if (indice in nuevas.indices) nuevas[indice] = valor
            it.copy(respuestas = nuevas, errorRespuestas = null, errorGeneral = null)
        }
    }

    fun verificar(): Boolean {
        val estado = _uiState.value
        val persona = persona ?: return false
        if (estado.elegidas.any { it !in estado.catalogo.indices }) {
            _uiState.update { it.copy(errorRespuestas = "Elige tu pregunta de seguridad.") }
            return false
        }
        if (estado.elegidas.toSet().size != estado.elegidas.size) {
            _uiState.update { it.copy(errorRespuestas = "Elige preguntas distintas.") }
            return false
        }
        if (estado.respuestas.any { it.isBlank() }) {
            _uiState.update { it.copy(errorRespuestas = "Responde tu pregunta.") }
            return false
        }
        val pares = estado.elegidas.mapIndexed { i, posicion -> estado.catalogo[posicion].id to estado.respuestas[i] }
        if (!repositorio.verificarRespuestas(persona.id, persona.rut, pares)) {
            _uiState.update {
                it.copy(errorRespuestas = "La pregunta o la respuesta no coinciden. Revísalas.")
            }
            return false
        }
        _uiState.update { it.copy(paso = PasoRecuperar.CLAVE, errorRespuestas = null) }
        return true
    }

    fun onClaveCambia(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null) }
    }

    fun onRepetirCambia(valor: String) {
        _uiState.update { it.copy(repetir = valor, errorRepetir = null) }
    }

    fun onAlternarClave() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    fun cambiar(): Boolean {
        val estado = _uiState.value
        val persona = persona ?: return false
        mensajeDeClave(estado.clave)?.let {
            _uiState.update { s -> s.copy(errorClave = it) }
            return false
        }
        if (estado.clave != estado.repetir) {
            _uiState.update { it.copy(errorRepetir = "Las dos claves no coinciden.") }
            return false
        }
        repositorio.cambiarClave(persona.id, Clave.hash(persona.rut, estado.clave))
        _uiState.update { it.copy(paso = PasoRecuperar.LISTO, errorClave = null, errorRepetir = null) }
        return true
    }

    fun volver(): Boolean = when (_uiState.value.paso) {
        PasoRecuperar.RUT -> false
        PasoRecuperar.PREGUNTAS -> { _uiState.update { it.copy(paso = PasoRecuperar.RUT) }; true }
        PasoRecuperar.CLAVE -> { _uiState.update { it.copy(paso = PasoRecuperar.PREGUNTAS) }; true }
        PasoRecuperar.LISTO -> true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}