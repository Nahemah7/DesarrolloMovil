package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioSesionesRemoto
import com.example.rinconsur.data.remoto.TokenPreferencias
import kotlinx.coroutines.launch
import com.example.rinconsur.model.DetalleSesion
import com.example.rinconsur.model.EstadoSesion
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class DetalleSesionViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val token = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)
    private val idRevisor: Long = sesion.leer()

    private val _detalle = MutableStateFlow<DetalleSesion?>(null)
    val detalle: StateFlow<DetalleSesion?> = _detalle.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    fun cargar(idSesion: Long) {
        _detalle.value = repositorio.detalleDeSesion(idSesion)
    }

    fun recargar(idSesion: Long) = cargar(idSesion)

    fun validar(idSesion: Long, codigo: String) {
        val guardado = token.leer()
        if (guardado == null) {
            validarLocal(idSesion, codigo)
            return
        }
        val idRemoto = repositorio.idRemotoDeSesion(idSesion)
        if (idRemoto == null) {
            validarLocal(idSesion, codigo)
            return
        }
        viewModelScope.launch {
            val remoto = RepositorioSesionesRemoto(direccion.leer(), guardado)
            if (remoto.validar(idRemoto)) {

                repositorio.revisarSesion(idSesion, EstadoSesion.VALIDADA, idRevisor)
                cargar(idSesion)
                _aviso.tryEmit("Sesión " + codigo + " validada en el servidor.")
            } else {
                validarLocal(idSesion, codigo)
            }
        }
    }

    private fun validarLocal(idSesion: Long, codigo: String) {
        val filas = repositorio.revisarSesion(idSesion, EstadoSesion.VALIDADA, idRevisor)
        cargar(idSesion)
        _aviso.tryEmit(
            if (filas > 0) "Sesión " + codigo + " validada en el teléfono: se sincronizará cuando haya red."
            else "No se pudo validar " + codigo + ": la sesión cambió de estado."
        )
    }

    val accesos = repositorio.accesos()
    val observadores = repositorio.observadores()

    fun editar(idSesion: Long, idAcceso: Long, horaInicio: String, horaFin: String, idObservador: Long) {
        val d = _detalle.value ?: return
        if (minutosDe(horaFin) - minutosDe(horaInicio) !in 30..60) {
            _aviso.tryEmit("El horario debe durar entre 30 y 60 minutos.")
            return
        }
        val choque = repositorio.sesionesDeFecha(d.sesion.fecha).firstOrNull { otra ->
            otra.id != idSesion && (otra.idAcceso == idAcceso || otra.idObservador == idObservador) &&
                otra.horaInicioProg < horaFin && otra.horaFinProg > horaInicio
        }
        if (choque != null) {
            _aviso.tryEmit("Ese horario se cruza con la sesión " + choque.codigo + ".")
            return
        }
        val filas = repositorio.actualizarSesionProgramada(idSesion, idAcceso, horaInicio, horaFin, idObservador)
        if (filas == 0) {
            _aviso.tryEmit("Solo se cambia una sesión programada.")
            cargar(idSesion)
            return
        }
        cargar(idSesion)
        val guardado = token.leer()
        val idRemoto = repositorio.idRemotoDeSesion(idSesion)
        if (guardado == null || idRemoto == null) {
            _aviso.tryEmit("Cambio guardado en este teléfono: se enviará al servidor cuando haya red.")
            return
        }
        viewModelScope.launch {
            val nombre = repositorio.colaboradorPorId(idObservador)
                ?.let { (it.nombre + " " + it.apellido).trim() }
            val enviado = RepositorioSesionesRemoto(direccion.leer(), guardado)
                .editar(idRemoto, idAcceso, horaInicio, horaFin, idObservador, nombre)
            _aviso.tryEmit(
                if (enviado) "Cambio guardado en el servidor."
                else "Cambio guardado en este teléfono: el servidor no respondió."
            )
        }
    }

    private fun minutosDe(hora: String): Int {
        val partes = hora.split(":")
        return partes[0].toInt() * 60 + partes.getOrElse(1) { "0" }.toInt()
    }

    fun puedeRevisar(): Boolean {
        val d = _detalle.value ?: return false
        return d.sesion.estado.esperaRevision && d.sesion.idObservador != idRevisor && d.sesion.sePuedeValidar
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
