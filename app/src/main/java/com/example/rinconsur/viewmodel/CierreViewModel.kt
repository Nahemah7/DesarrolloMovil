package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.ResumenSesion
import com.example.rinconsur.model.SesionResumen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Cierre de la sesion: el resumen de lo registrado y el envio a validacion. */
class CierreViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val idObservador = sesion.leer()
    /**
     * Solo la sesion EN CURSO. Antes, si no habia ninguna en curso, tomaba cualquier sesion del dia:
     * al no tener sesion propia aparecia la sembrada S-0001 lista para enviar a validacion.
     */
    private var sesionActual: SesionResumen? = repositorio.sesionEnCursoDe(idObservador)

    private val _resumen = MutableStateFlow(repositorio.resumenDeSesion(sesionActual?.id ?: 0L))
    val resumen: StateFlow<ResumenSesion> = _resumen.asStateFlow()

    private val _codigo = MutableStateFlow(sesionActual?.codigo ?: "")
    val codigo: StateFlow<String> = _codigo.asStateFlow()

    val sePuedeValidar: Boolean get() = _resumen.value.total >= FlujoRepository.MINIMO_REGISTROS

    /** Los registros que faltan para poder enviar; 0 cuando ya se puede. */
    val faltan: Int get() = (FlujoRepository.MINIMO_REGISTROS - _resumen.value.total).coerceAtLeast(0)

    val minimo: Int get() = FlujoRepository.MINIMO_REGISTROS

    /** Lo que se le dice al observador cuando aprieta enviar sin llegar al minimo. */
    private val _aviso = MutableStateFlow<String?>(null)
    val aviso: StateFlow<String?> = _aviso.asStateFlow()

    /** La sesion ya se envio: la pantalla deja de ofrecer el envio y no se manda dos veces. */
    private val _yaEnviada = MutableStateFlow(false)
    val yaEnviada: StateFlow<Boolean> = _yaEnviada.asStateFlow()

    /**
     * Se relee al volver a la pantalla. La sesion se puede enviar desde otra pestaña o desde la pila
     * de navegacion: si no se relee, esta pantalla seguia ofreciendo "Enviar a validación" sobre una
     * sesion ya enviada.
     */
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

    fun cerrar(): Boolean {
        // Estado real de la base antes de enviar: la sesion pudo cerrarse por fuera.
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
        val filas = repositorio.cerrarSesion(enCurso.id, java.time.LocalTime.now().toString().take(8))
        if (filas == 0) {
            // La base no la cerro porque ya no estaba en curso: no se envia dos veces.
            _yaEnviada.value = true
            _aviso.value = "Esta sesión ya fue enviada a validación."
            return false
        }
        _yaEnviada.value = true
        _aviso.value = null
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
