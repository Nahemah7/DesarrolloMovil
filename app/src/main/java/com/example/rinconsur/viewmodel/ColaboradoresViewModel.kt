package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.CodigoTemporal
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.Colaborador
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ColaboradoresViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    private val _colaboradores = MutableStateFlow<List<Colaborador>>(emptyList())
    val colaboradores: StateFlow<List<Colaborador>> = _colaboradores.asStateFlow()

    private val _codigo = MutableStateFlow<String?>(null)
    val codigo: StateFlow<String?> = _codigo.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    private val _deEjemplo = MutableStateFlow(false)
    val deEjemplo: StateFlow<Boolean> = _deEjemplo.asStateFlow()

    fun cargar() {
        _colaboradores.value = repositorio.colaboradores()
        _deEjemplo.value = repositorio.datosDeEjemplo()
    }

    fun empezarDeCero(): Boolean {
        val borrado = repositorio.borrarDatosDeEjemplo()
        cargar()
        _aviso.tryEmit(
            if (borrado) "Listo: desde ahora solo quedan los datos que cargues tú."
            else "No se pudieron borrar los datos de ejemplo."
        )
        return borrado
    }

    fun regenerarCodigo(colaborador: Colaborador) {
        if (!colaborador.debeCambiarClave) {
            _aviso.tryEmit("Esa persona ya definió su clave: no corresponde un código temporal.")
            return
        }
        val nuevo = CodigoTemporal.generar()
        if (repositorio.regenerarCodigo(colaborador.id, colaborador.rut, nuevo)) {
            _codigo.value = nuevo
            cargar()
        } else {
            _aviso.tryEmit("No se pudo generar el código.")
        }
    }

    fun cerrarCodigo() {
        _codigo.value = null
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
