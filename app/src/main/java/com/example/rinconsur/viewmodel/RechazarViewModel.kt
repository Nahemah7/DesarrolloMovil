package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.MotivoRechazo
import com.example.rinconsur.model.RechazarUiState
import com.example.rinconsur.data.TEXTO_MAXIMO
import com.example.rinconsur.data.mensajeDeTextoLibre
import com.example.rinconsur.model.textoDeRechazo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RechazarViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val catalogoMotivos: List<MotivoRechazo> = MotivoRechazo.entries

    private val _uiState = MutableStateFlow(RechazarUiState())
    val uiState: StateFlow<RechazarUiState> = _uiState.asStateFlow()

    fun cargar(idSesion: Long) {
        if (idSesion <= 0L || _uiState.value.codigo.isNotEmpty()) return
        val sesion = repositorio.sesion(idSesion) ?: return
        val acceso = repositorio.accesos().firstOrNull { it.id == sesion.idAcceso }
        _uiState.value = RechazarUiState(
            codigo = sesion.codigo,
            acceso = (acceso?.codigo ?: "") + " " + (acceso?.nombre ?: ""),
            observador = sesion.nombreObservador,
            hora = sesion.horaInicioCorta + " - " + sesion.horaFinCorta,
            registros = sesion.registrosValidos,
            pctTransito = sesion.pctTransito
        )
    }

    fun onMotivo(motivo: MotivoRechazo) {
        _uiState.update {
            val nuevos = if (it.motivos.contains(motivo)) it.motivos - motivo else it.motivos + motivo
            it.copy(
                motivos = nuevos,
                errorMotivos = null,
                otroMotivo = if (nuevos.contains(MotivoRechazo.OTRO)) it.otroMotivo else "",
                errorOtro = null
            )
        }
    }

    fun onOtroMotivo(texto: String) {
        _uiState.update { it.copy(otroMotivo = texto, errorOtro = null) }
    }

    fun rechazar(idSesion: Long, idRevisor: Long): Boolean {
        val estado = _uiState.value
        val errorMotivos = if (estado.motivos.isEmpty()) "Elige al menos un motivo" else null

        val motivo = textoDeRechazo(estado.motivos, estado.otroMotivo)

        val errorOtro = if (!estado.exigeOtro) null else
            mensajeDeTextoLibre(estado.otroMotivo, "Cuenta el motivo")
                ?: if (motivo.length > TEXTO_MAXIMO)
                    "Con los motivos marcados el texto pasa de $TEXTO_MAXIMO caracteres: acorta el detalle"
                else null

        if (errorMotivos != null || errorOtro != null) {
            _uiState.update { it.copy(errorMotivos = errorMotivos, errorOtro = errorOtro) }
            return false
        }

        repositorio.revisarSesion(idSesion, EstadoSesion.RECHAZADA, idRevisor, motivo)
        _uiState.update { it.copy(rechazada = true) }
        return true
    }

    val motivos: List<MotivoRechazo> get() = catalogoMotivos

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
