package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.CargaDia
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Carga de ingresos y boletas del día: once bloques, con el reparto por acceso opcional.
class CargaDiaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val idUsuario: Long = sesion.leer()

    private val _carga = MutableStateFlow<CargaDia?>(null)
    val carga: StateFlow<CargaDia?> = _carga.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    fun cargar() {
        _carga.value = repositorio.cargaDelDia()
    }

    fun onIngresos(idBloque: Long, valor: String) =
        _carga.update { it?.conIngresos(idBloque, soloNumero(valor)) }

    fun onBoletas(idBloque: Long, valor: String) =
        _carga.update { it?.conBoletas(idBloque, soloNumero(valor)) }

    fun onDetalle(idBloque: Long, idAcceso: Long, valor: String) =
        _carga.update { it?.conDetalle(idBloque, idAcceso, soloNumero(valor)) }

    fun alternarDetalle(idBloque: Long) {
        val actual = _carga.value ?: return
        val bloque = actual.bloque(idBloque) ?: return
        if (bloque.detallado) {
            _carga.value = actual.alternarDetalle(idBloque)
            return
        }
        val total = bloque.ingresosNumero ?: run {
            _aviso.tryEmit("Escribe el total del bloque antes de repartirlo por acceso.")
            return
        }
        // Se abre el reparto con el total puesto en el primer acceso: el resto se completa a mano
        // y la suma tiene que cuadrar antes de guardar.
        val primero = actual.accesos.firstOrNull() ?: return
        _carga.value = actual
            .alternarDetalle(idBloque)
            .conDetalle(idBloque, primero.id, total.toString())
    }

    fun guardar(): Boolean {
        val carga = _carga.value ?: return false
        val faltas = carga.errores
        if (faltas.isNotEmpty()) {
            _aviso.tryEmit(faltas.first())
            return false
        }
        if (!carga.puedeGuardar) {
            _aviso.tryEmit("No hay nada que guardar todavía.")
            return false
        }
        val guardado = repositorio.guardarCargaDelDia(carga, idUsuario)
        if (!guardado) {
            _aviso.tryEmit("No pudimos guardar la carga del día.")
            return false
        }
        cargar()
        val aparte = if (carga.bloquesAPartar > 0)
            " · " + carga.bloquesAPartar + " bloque(s) con un solo dato" else ""
        _aviso.tryEmit(
            "Día guardado: " + carga.totalIngresos + " ingresos y " + carga.totalBoletas +
                " boletas, conversión " + carga.conversionTexto + "." + aparte
        )
        return true
    }

    private fun soloNumero(valor: String): String = valor.filter { it.isDigit() }.take(5)

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
