package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioSesionesRemoto
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.model.FuenteDatos
import kotlinx.coroutines.launch
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionesUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class SesionesViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val token = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private var fechaElegida: String = repositorio.fechaHoy()

    private val formatoFecha = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
    private val formatoLargo = java.text.SimpleDateFormat("EEEE d 'de' MMMM", java.util.Locale("es", "CL"))

    private val idRevisor: Long = sesion.leer()

    private val _uiState = MutableStateFlow(SesionesUiState(miId = idRevisor))
    val uiState: StateFlow<SesionesUiState> = _uiState.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    val accesos: List<Acceso> = repositorio.accesos()
    val observadores: List<Colaborador> = repositorio.observadores()

    init {
        recargar()
        sincronizar()
    }

    fun recargar() {
        val pendientes = repositorio.bloquesPendientes(fechaElegida)
        val anterior = _uiState.value
        _uiState.value = SesionesUiState(
            sesiones = repositorio.sesionesDeFecha(fechaElegida),
            porValidar = repositorio.sesionesPorValidar(),
            deLaSemana = repositorio.sesionesDeLosUltimosDias(fechaElegida, 7),
            bloquesPendientes = pendientes.size,
            pendienteMasAtrasado = pendientes.firstOrNull() ?: "",
            filtro = anterior.filtro,
            idAcceso = anterior.idAcceso,
            idObservador = anterior.idObservador,
            soloMias = anterior.soloMias,
            miId = idRevisor,
            fecha = fechaElegida,
            fechaLarga = fechaLarga(fechaElegida)
        )
        sincronizar()
    }

    // La pantalla se pinta con la copia local y, si hay sesión en el servidor, se reemplaza con
    // lo que responde el gateway. Sin red no se pierde nada: queda lo local y el aviso de fuente.
    private fun sincronizar() {
        val guardado = token.leer() ?: return
        val fecha = fechaElegida
        viewModelScope.launch {
            val remoto = RepositorioSesionesRemoto(direccion.leer(), guardado)
            val delDia = remoto.sesionesDeFecha(fecha)
            val pendientes = remoto.porValidar()
            if (delDia == null || pendientes == null || fecha != fechaElegida) return@launch
            _uiState.value = _uiState.value.copy(
                sesiones = delDia,
                porValidar = pendientes,
                fuente = FuenteDatos.EN_LINEA
            )
        }
    }

    fun diaAnterior() = moverDia(-1)

    fun diaSiguiente() = moverDia(1)

    fun irAHoy() {
        fechaElegida = repositorio.fechaHoy()
        _uiState.value = _uiState.value.copy(filtro = "Día")
        recargar()
    }

    fun elegirFecha(fecha: String) {
        fechaElegida = fecha
        _uiState.value = _uiState.value.copy(filtro = "Día")
        recargar()
    }

    private fun moverDia(dias: Int) {
        val calendario = java.util.Calendar.getInstance()
        calendario.time = formatoFecha.parse(fechaElegida) ?: java.util.Date()
        calendario.add(java.util.Calendar.DAY_OF_MONTH, dias)
        elegirFecha(formatoFecha.format(calendario.time))
    }

    private fun fechaLarga(fecha: String): String =
        formatoFecha.parse(fecha)?.let { formatoLargo.format(it) } ?: fecha

    fun filtrar(filtro: String) {
        _uiState.value = _uiState.value.copy(filtro = filtro)
    }

    fun filtrarAcceso(idAcceso: Long?) {
        val actual = _uiState.value.idAcceso
        _uiState.value = _uiState.value.copy(idAcceso = if (actual == idAcceso) null else idAcceso)
    }

    fun filtrarObservador(idObservador: Long?) {
        val actual = _uiState.value.idObservador
        _uiState.value = _uiState.value.copy(idObservador = if (actual == idObservador) null else idObservador)
    }

    fun alternarSoloMias() {
        _uiState.value = _uiState.value.copy(soloMias = !_uiState.value.soloMias)
    }

    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(idAcceso = null, idObservador = null, soloMias = false)
    }

    fun validar(idSesion: Long, codigo: String) {
        val guardado = token.leer()
        if (guardado == null) {
            validarLocal(idSesion, codigo)
            return
        }
        viewModelScope.launch {
            val remoto = RepositorioSesionesRemoto(direccion.leer(), guardado)
            if (remoto.validar(idSesion)) {
                repositorio.revisarSesion(idSesion, EstadoSesion.VALIDADA, idRevisor)
                _aviso.tryEmit("Sesión " + codigo + " validada en el servidor.")
                recargar()
            } else {
                // Sin red o rechazada por el servicio: se intenta en la copia local y se avisa.
                validarLocal(idSesion, codigo)
            }
        }
    }

    private fun validarLocal(idSesion: Long, codigo: String) {
        val filas = repositorio.revisarSesion(idSesion, EstadoSesion.VALIDADA, idRevisor)
        recargar()
        _aviso.tryEmit(
            if (filas > 0) "Sesión " + codigo + " validada en el teléfono: se enviará al servidor cuando haya red."
            else "No se pudo validar " + codigo + ": la sesión cambió de estado."
        )
    }

    fun rechazar(idSesion: Long, motivo: String) {
        repositorio.revisarSesion(idSesion, EstadoSesion.RECHAZADA, idRevisor, motivo)
        recargar()
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
