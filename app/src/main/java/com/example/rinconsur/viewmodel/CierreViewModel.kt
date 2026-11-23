package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioSesionesRemoto
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.rinconsur.model.ResumenSesion
import com.example.rinconsur.model.SesionResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CierreViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private val idObservador = sesion.leer()

    private var sesionActual: SesionResumen? = repositorio.sesionEnCursoDe(idObservador)

    private val _resumen = MutableStateFlow(repositorio.resumenDeSesion(sesionActual?.id ?: 0L))
    val resumen: StateFlow<ResumenSesion> = _resumen.asStateFlow()

    private val _codigo = MutableStateFlow(sesionActual?.codigo ?: "")
    val codigo: StateFlow<String> = _codigo.asStateFlow()

    val sePuedeValidar: Boolean get() = _resumen.value.total >= FlujoRepository.MINIMO_REGISTROS

    val faltan: Int get() = (FlujoRepository.MINIMO_REGISTROS - _resumen.value.total).coerceAtLeast(0)

    val minimo: Int get() = FlujoRepository.MINIMO_REGISTROS

    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    private val _yaEnviada = MutableStateFlow(false)
    val yaEnviada: StateFlow<Boolean> = _yaEnviada.asStateFlow()

    fun releer() {
        val enCurso = repositorio.sesionEnCursoDe(idObservador)
        if (enCurso == null) {
            if (sesionActual != null) _yaEnviada.value = true
        } else {
            sesionActual = enCurso
            _resumen.value = repositorio.resumenDeSesion(enCurso.id)
            _codigo.value = enCurso.codigo
            _yaEnviada.value = false
        }
    }

    fun limpiarAviso() {
        _aviso.value = null
    }

    fun cerrar(): Boolean {
        val enCurso = repositorio.sesionEnCursoDe(idObservador)
        if (enCurso == null) {
            sesionActual = null
            _yaEnviada.value = true
            _aviso.value = "Esta sesión ya fue enviada a validación."
            return false
        }
        sesionActual = enCurso
        _resumen.value = repositorio.resumenDeSesion(enCurso.id)
        val registros = _resumen.value.total
        if (registros < FlujoRepository.MINIMO_REGISTROS) {
            _aviso.value = "Sigue registrando: llevas " + registros + " de " + FlujoRepository.MINIMO_REGISTROS +
                " y faltan " + (FlujoRepository.MINIMO_REGISTROS - registros) + ". Los datos no se pierden."
            return false
        }
        val hora = java.time.LocalTime.now().toString().take(8)
        val filas = repositorio.cerrarSesion(enCurso.id, hora)
        if (filas == 0) {
            _yaEnviada.value = true
            _aviso.value = "Esta sesión ya fue enviada a validación."
            return false
        }
        _yaEnviada.value = true
        _aviso.value = null
        enviarCierre(enCurso.id, hora)
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }

    private fun enviarCierre(idLocal: Long, hora: String) {
        val guardado = tokens.leer() ?: return
        val idRemoto = repositorio.idRemotoDeSesion(idLocal) ?: return
        TrabajoDeFondo.alcance.launch {
            val enviado = RepositorioSesionesRemoto(direccion.leer(), guardado).cerrar(idRemoto, hora)
            if (!enviado) {
                _aviso.value = "Quedó cerrada en este teléfono: el servidor no respondió. " +
                    "Vuelve a enviarla desde Sin señal."
            }
        }
    }
}
