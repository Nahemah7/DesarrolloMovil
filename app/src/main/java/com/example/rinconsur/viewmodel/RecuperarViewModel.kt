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

/**
 * Recuperar la clave sin correo: RUT, la pregunta de seguridad de esa persona y una clave
 * nueva. La persona se guarda en memoria entre pasos, no en el estado: la pantalla no la necesita.
 */
class RecuperarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    private val _uiState = MutableStateFlow(RecuperarUiState())
    val uiState: StateFlow<RecuperarUiState> = _uiState.asStateFlow()

    private var persona: Colaborador? = null

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, errorGeneral = null) }
    }

    /** Paso 1: revisa el numero escrito y busca a la persona con sus preguntas. */
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
        val preguntas = encontrado?.let { repositorio.preguntasDe(it.id) } ?: emptyList()
        if (encontrado == null || preguntas.isEmpty()) {
            // Un solo mensaje: no se revela si el RUT existe ni si tiene preguntas guardadas.
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
                preguntas = preguntas,
                respuestas = List(preguntas.size) { "" },
                errorGeneral = null
            )
        }
        return true
    }

    fun onRespuestaCambia(indice: Int, valor: String) {
        _uiState.update {
            val nuevas = it.respuestas.toMutableList()
            if (indice in nuevas.indices) nuevas[indice] = valor
            it.copy(respuestas = nuevas, errorRespuestas = null, errorGeneral = null)
        }
    }

    /** Paso 2: comprueba las respuestas contra las guardadas (hasheadas). */
    fun verificar(): Boolean {
        val estado = _uiState.value
        val persona = persona ?: return false
        if (estado.respuestas.any { it.isBlank() }) {
            _uiState.update { it.copy(errorRespuestas = "Responde tu pregunta.") }
            return false
        }
        val pares = estado.preguntas.mapIndexed { i, p -> p.id to estado.respuestas[i] }
        if (!repositorio.verificarRespuestas(persona.id, persona.rut, pares)) {
            _uiState.update { it.copy(errorRespuestas = "La respuesta no coincide. Revísala.") }
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

    /** Paso 3: valida la clave con la regla acordada y la guarda hasheada. */
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

    /** Volver al paso anterior, sin perder lo escrito. */
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
