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
        val local = localDe(idSesion)
        val encontrado = repositorio.detalleDeSesion(local)
        _detalle.value = encontrado
        val guardado = token.leer() ?: return
        val idRemoto = repositorio.idRemotoDeSesion(local) ?: idSesion
        viewModelScope.launch {
            val remota = RepositorioSesionesRemoto(direccion.leer(), guardado).porId(idRemoto) ?: return@launch
            val actual = _detalle.value ?: return@launch
            _detalle.value = actual.copy(
                sesion = actual.sesion.copy(
                    estado = remota.estado,
                    registrosValidos = remota.registrosValidos,
                    inicioReal = remota.inicioReal,
                    finReal = remota.finReal
                ),
                registrosValidos = remota.registrosValidos
            )
        }
    }

    private fun localDe(idSesion: Long): Long =
        repositorio.idLocalDeRemota(idSesion) ?: idSesion

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

    fun puedeRevisar(): Boolean {
        val d = _detalle.value ?: return false
        return d.sesion.estado.esperaRevision && d.sesion.idObservador != idRevisor && d.sesion.sePuedeValidar
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
