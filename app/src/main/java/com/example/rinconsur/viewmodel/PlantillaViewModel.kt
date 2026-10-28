package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.Plantilla
import com.example.rinconsur.data.Plantillas
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.RangoEtario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PlantillaUiState(
    val numero: Int = 1,
    val clasificacion: Clasificacion = Clasificacion.TRANSITO,
    val rangoEtario: RangoEtario? = null,
    val compania: Compania = Compania.SOLO,
    val comprasOtraTienda: Boolean = false,
    val destino: Long? = null,
    val accesos: List<com.example.rinconsur.model.Acceso> = emptyList(),
    val guardada: Boolean = false,
    val tenia: Boolean = false,
    val mensaje: String? = null
)

class PlantillaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val plantillas = Plantillas(application)

    private val _uiState = MutableStateFlow(PlantillaUiState())
    val uiState: StateFlow<PlantillaUiState> = _uiState.asStateFlow()

    fun cargar(numero: Int) {
        val guardada = plantillas.leer(numero)
        _uiState.update {
            it.copy(
                numero = numero,
                clasificacion = guardada?.clasificacion ?: Clasificacion.TRANSITO,
                rangoEtario = guardada?.rangoEtario,
                compania = guardada?.compania ?: Compania.SOLO,
                comprasOtraTienda = guardada?.comprasOtraTienda ?: false,
                destino = guardada?.destino,
                accesos = repositorio.accesos(),
                tenia = guardada != null,
                guardada = false,
                mensaje = null
            )
        }
    }

    fun onClasificacion(c: Clasificacion) = _uiState.update { it.copy(clasificacion = c, guardada = false) }
    fun onRango(r: RangoEtario) = _uiState.update { it.copy(rangoEtario = r, guardada = false) }
    fun onCompania(c: Compania) = _uiState.update { it.copy(compania = c, guardada = false) }
    fun onCompras(v: Boolean) = _uiState.update { it.copy(comprasOtraTienda = v, guardada = false) }
    fun onDestino(id: Long) = _uiState.update { it.copy(destino = id, guardada = false) }

    fun guardar(): Boolean {
        val e = _uiState.value
        if (e.rangoEtario == null) {
            _uiState.update { it.copy(mensaje = "Elige el tramo etario antes de guardar.") }
            return false
        }
        plantillas.guardar(
            e.numero,
            Plantilla(e.clasificacion, e.rangoEtario, e.compania, e.comprasOtraTienda, e.destino)
        )
        _uiState.update { it.copy(guardada = true, tenia = true, mensaje = "Plantilla " + e.numero + " guardada.") }
        return true
    }

    fun borrar() {
        val numero = _uiState.value.numero
        plantillas.borrar(numero)
        _uiState.update { it.copy(guardada = false, tenia = false, mensaje = "Plantilla " + numero + " borrada.") }
    }

    fun limpiarMensaje() = _uiState.update { it.copy(mensaje = null) }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
