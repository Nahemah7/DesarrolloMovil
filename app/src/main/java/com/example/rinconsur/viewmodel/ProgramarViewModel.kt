package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionSesionNueva
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.model.ProgramarUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ProgramarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)
    private val idProgramador: Long = sesion.leer()

    private val _uiState = MutableStateFlow(ProgramarUiState())
    val uiState: StateFlow<ProgramarUiState> = _uiState.asStateFlow()

    init {

        val fecha = repositorio.fechaHoy()
        _uiState.update {
            it.copy(
                fecha = fecha,
                accesos = repositorio.accesos(),
                observadores = repositorio.observadores(),
                sesionesDeLaFecha = repositorio.sesionesDeFecha(fecha)
            )
        }
    }

    fun cargarParaEditar(idSesion: Long) {
        val detalle = repositorio.detalleDeSesion(idSesion)?.sesion ?: return
        if (detalle.estado != com.example.rinconsur.model.EstadoSesion.PROGRAMADA) return
        _uiState.update {
            it.copy(
                fecha = detalle.fecha,
                idAcceso = detalle.idAcceso,
                horaInicio = detalle.horaInicioCorta,
                horaFin = detalle.horaFinCorta,
                idObservador = detalle.idObservador,
                sesionesDeLaFecha = repositorio.sesionesDeFecha(detalle.fecha),
                codigo = detalle.codigo,
                editando = true,
                idEditando = idSesion,
                accesoOriginal = detalle.idAcceso,
                observadorOriginal = detalle.idObservador,
                inicioOriginal = detalle.horaInicioCorta,
                finOriginal = detalle.horaFinCorta,
                error = null
            )
        }
    }

    fun onAcceso(id: Long) = _uiState.update { it.copy(idAcceso = id, error = null, programada = "", codigo = "") }

    fun onObservador(id: Long) = _uiState.update { it.copy(idObservador = id, error = null, programada = "", codigo = "") }

    fun onHoraInicio(hora: String) = _uiState.update {
        it.copy(horaInicio = hora, error = null, programada = "", codigo = "")
    }

    fun onHoraFin(hora: String) = _uiState.update {
        it.copy(horaFin = hora, error = null, programada = "", codigo = "")
    }

    val horas: List<String> = (10 * 60..20 * 60 step 10).map { "%02d:%02d".format(it / 60, it % 60) }

    fun limpiarError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun publicarEnServidor(
        codigo: String,
        idAcceso: Long,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        idObservador: Long,
        idEditando: Long = 0L
    ): String {
        val token = tokens.leer()
            ?: return "Quedó guardada en este teléfono: no hay sesión iniciada en el servidor. " +
                "Cierra sesión y vuelve a entrar para enviarla."
        val observador = repositorio.colaboradorPorId(idObservador)
        val nombre = observador?.let { (it.nombre + " " + it.apellido).trim() }
        com.example.rinconsur.data.remoto.TrabajoDeFondo.alcance.launch {
            runCatching {
                val cliente = ClienteRemoto(direccion.leer())
                if (idEditando > 0) {
                    val idRemoto = repositorio.idRemotoDeSesion(idEditando)
                    if (idRemoto == null) {
                        _uiState.update {
                            it.copy(error = "Cambio guardado en este teléfono: la sesión aún no está en el servidor.")
                        }
                        return@launch
                    }
                    val cambiado = com.example.rinconsur.data.remoto.RepositorioSesionesRemoto(direccion.leer(), token)
                        .editar(idRemoto, idAcceso, horaInicio, horaFin, idObservador, nombre)
                    _uiState.update {
                        it.copy(
                            error = if (cambiado) null else "Cambio guardado en este teléfono: el servidor no respondió.",
                            sesionesDeLaFecha = repositorio.sesionesDeFecha(fecha)
                        )
                    }
                    return@launch
                }
                val respuesta = cliente.medicion.programar(
                    "Bearer " + token,
                    PeticionSesionNueva(
                        idAcceso = idAcceso.toInt(),
                        fecha = fecha,
                        horaInicio = horaInicio,
                        horaFin = horaFin,
                        idObservador = idObservador.toInt(),
                        observadorNombre = nombre
                    )
                )
                val cuerpo = respuesta.body()
                val idRemoto = cuerpo?.id
                if (respuesta.isSuccessful && idRemoto != null) {
                    val codigoServicio = cuerpo.codigo ?: codigo
                    repositorio.adoptarSesionDelServicio(
                        repositorio.idDeSesionPorCodigo(codigo),
                        idRemoto.toLong(),
                        codigoServicio
                    )
                    _uiState.update {
                        it.copy(codigo = codigoServicio, sesionesDeLaFecha = repositorio.sesionesDeFecha(fecha))
                    }
                } else {
                    _uiState.update {
                        it.copy(error = "Quedó guardada en este teléfono: el servidor respondió " + respuesta.code() + ".")
                    }
                }
            }.onFailure { falla ->
                _uiState.update {
                    it.copy(error = "Quedó guardada en este teléfono: " + (falla.message ?: "sin conexión") + ".")
                }
            }
        }
        return ""
    }

    fun programar(): Boolean {
        val e = _uiState.value
        val problema = when {
            e.idAcceso == null -> "Elige el acceso donde se va a contar."
            e.idObservador == null -> "Elige quién va a contar."
            !e.duracionValida -> "El horario debe durar entre 30 y 60 minutos."
            e.choque != null -> textoDeChoque(e.choque!!, e)
            else -> null
        }
        if (problema != null) {
            _uiState.update { it.copy(error = problema) }
            return false
        }
        val idAcceso = e.idAcceso!!
        val idObservador = e.idObservador!!
        val editando = e.idEditando
        val codigo = if (editando > 0) {
            repositorio.actualizarSesionProgramada(editando, idAcceso, e.horaInicio, e.horaFin, idObservador)
            e.codigo
        } else {
            repositorio.programarSesion(idAcceso, e.fecha, e.horaInicio, e.horaFin, idObservador, idProgramador)
        }
        val aviso = publicarEnServidor(codigo, idAcceso, e.fecha, e.horaInicio, e.horaFin, idObservador, editando)
        _uiState.update {
            it.copy(
                error = aviso,
                codigo = codigo,
                programada = nombreDe(e, idObservador),
                sesionesDeLaFecha = repositorio.sesionesDeFecha(e.fecha),
                idAcceso = null,
                idObservador = null
            )
        }
        return true
    }

    private fun nombreDe(e: ProgramarUiState, idObservador: Long): String =
        e.observadores.firstOrNull { it.id == idObservador }?.nombre ?: "el observador"

    private fun textoDeChoque(choque: com.example.rinconsur.model.SesionResumen, e: ProgramarUiState): String {
        val donde = if (choque.idAcceso == e.idAcceso) {
            e.accesos.firstOrNull { it.id == choque.idAcceso }?.nombre ?: "Ese acceso"
        } else {
            e.observadores.firstOrNull { it.id == choque.idObservador }?.nombre ?: "Esa persona"
        }
        return donde + " ya tiene una sesión de " + choque.horaInicioCorta + " a " + choque.horaFinCorta + ": se cruza."
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
