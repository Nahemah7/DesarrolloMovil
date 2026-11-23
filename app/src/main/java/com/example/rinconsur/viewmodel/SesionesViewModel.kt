package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioSesionesRemoto
import com.example.rinconsur.data.remoto.SincronizadorSesiones
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.model.FuenteDatos
import com.example.rinconsur.model.SesionResumen
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
        val haySesion = token.leer() != null
        _uiState.value = SesionesUiState(
            sesiones = when {
                !haySesion -> repositorio.sesionesDeFecha(fechaElegida)
                anterior.fecha == fechaElegida -> anterior.sesiones
                else -> emptyList()
            },
            porValidar = if (haySesion) anterior.porValidar else repositorio.sesionesPorValidar(),
            fuente = if (haySesion) anterior.fuente else FuenteDatos.LOCAL,
            deLaSemana = repositorio.sesionesDeLosUltimosDias(fechaElegida, 7),
            abiertas = repositorio.sesionesAbiertasAntesDe(fechaElegida, repositorio.horaHaceHoras(2)),
            bloquesPendientes = pendientes.size,
            pendienteMasAtrasado = pendientes.firstOrNull() ?: "",
            filtro = anterior.filtro,
            idAcceso = anterior.idAcceso,
            busquedaObservador = anterior.busquedaObservador,
            miId = idRevisor,
            fecha = fechaElegida,
            fechaLarga = fechaLarga(fechaElegida)
        )
        sincronizar()
    }

    private fun aLocal(lista: List<SesionResumen>): List<SesionResumen> =
        lista.map { fila ->
            val esRemota = fila.idRemoto != null && fila.idRemoto == fila.id
            val local = if (esRemota) repositorio.idLocalDeRemota(fila.id) else null
            if (local != null) fila.copy(id = local) else fila
        }

    private fun sincronizar() {
        val guardado = token.leer()
        if (guardado == null) {
            _uiState.value = _uiState.value.copy(fuente = FuenteDatos.SIN_SESION)
            return
        }
        val fecha = fechaElegida
        viewModelScope.launch {
            val publicadas = SincronizadorSesiones(repositorio, token, direccion).publicarPendientes()
            if (publicadas > 0) {
                _uiState.value = _uiState.value.copy(
                    sesiones = repositorio.sesionesDeFecha(fechaElegida),
                    porValidar = repositorio.sesionesPorValidar()
                )
            }
            val remoto = RepositorioSesionesRemoto(direccion.leer(), guardado)
            val delDia = remoto.sesionesDeFecha(fecha)
            val pendientes = remoto.porValidar()
            if (delDia == null || pendientes == null || fecha != fechaElegida) return@launch
            repositorio.espejarSesiones(delDia + pendientes)
            _uiState.value = _uiState.value.copy(
                sesiones = aLocal(delDia),
                porValidar = aLocal(pendientes),
                deLaSemana = repositorio.sesionesDeLosUltimosDias(fechaElegida, 7),
                abiertas = repositorio.sesionesAbiertasAntesDe(fechaElegida, repositorio.horaHaceHoras(2)),
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

    fun buscarObservador(texto: String) {
        _uiState.value = _uiState.value.copy(busquedaObservador = texto)
    }


    fun limpiarFiltros() {
        _uiState.value = _uiState.value.copy(idAcceso = null, busquedaObservador = "")
    }

    fun validar(idSesion: Long, codigo: String) {
        val guardado = token.leer()
        if (guardado == null) {
            validarLocal(idSesion, codigo)
            return
        }
        viewModelScope.launch {
            val publicadas = SincronizadorSesiones(repositorio, token, direccion).publicarPendientes()
            if (publicadas > 0) {
                _uiState.value = _uiState.value.copy(
                    sesiones = repositorio.sesionesDeFecha(fechaElegida),
                    porValidar = repositorio.sesionesPorValidar()
                )
            }
            val remoto = RepositorioSesionesRemoto(direccion.leer(), guardado)
            if (remoto.validar(idSesion)) {
                repositorio.revisarSesion(idSesion, EstadoSesion.VALIDADA, idRevisor)
                _aviso.tryEmit("Sesión " + codigo + " validada en el servidor.")
                recargar()
            } else {

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
