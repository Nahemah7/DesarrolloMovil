package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Clave
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.Rut
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Inicio de sesion. En este prototipo no hay clave real: se valida el RUT contra la
 * base (los siete colaboradores del Anexo 9.2) y se recuerda quien entro. Cuando exista
 * la API, este es el unico lugar que cambia: la pantalla no se toca.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _usuario = MutableStateFlow<Colaborador?>(null)
    val usuario: StateFlow<Colaborador?> = _usuario.asStateFlow()

    /** Si ya habia alguien adentro (el telefono no se cerro bien), se recupera. */
    init {
        val guardado = sesion.leer()
        if (guardado > 0) _usuario.value = repositorio.colaboradorPorId(guardado)
    }

    fun onRutCambia(valor: String) {
        _uiState.update { it.copy(rut = valor, errorRut = null, errorGeneral = null) }
    }

    fun onClaveCambia(valor: String) {
        _uiState.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    fun onAlternarClave() {
        _uiState.update { it.copy(claveVisible = !it.claveVisible) }
    }

    /**
     * Valida y devuelve el rol que entro (o null si algo no cuadra).
     *
     * Dos niveles, a proposito distintos:
     *  1. El RUT mal escrito (formato o digito verificador) es un error del numero que la persona
     *     tipeo, se le dice claro y se le pide de nuevo. No revela nada del sistema: el digito
     *     verificador se calcula con el propio RUT.
     *  2. Todo lo que se compara con la base (el RUT no existe, la cuenta esta desactivada o la clave
     *     no coincide) responde con **un solo mensaje generico**. Si la app dijera "ese RUT no esta
     *     registrado", cualquiera podria probar RUTs y sacar la lista del personal; y si dijera
     *     "la contraseña no coincide" confirmaria que el RUT existe y que solo falta la clave.
     */
    fun entrar(): PerfilUsuario? {
        val rutEscrito = _uiState.value.rut
        val clave = _uiState.value.clave

        // Nivel 1: el numero que se escribio
        if (Rut.validar(rutEscrito) != null) {
            _uiState.update {
                it.copy(
                    errorRut = "El RUT no es válido. Revísalo y escríbelo de nuevo.",
                    errorClave = null,
                    errorGeneral = null
                )
            }
            return null
        }
        if (clave.isBlank()) {
            _uiState.update {
                it.copy(errorClave = "Escribe tu contraseña", errorGeneral = null)
            }
            return null
        }

        // Nivel 2: lo que se compara con la base (una sola respuesta, sin pistas)
        val encontrado = repositorio.colaboradorPorRut(Rut.formatear(rutEscrito))
            ?: repositorio.colaboradorPorRut(rutEscrito.trim())
        val claveCorrecta = encontrado != null &&
            Clave.hash(Rut.formatear(rutEscrito), clave) == encontrado.passwordHash
        if (encontrado == null || !encontrado.activo || !claveCorrecta) {
            _uiState.update {
                it.copy(
                    errorRut = null,
                    errorClave = null,
                    errorGeneral = "RUT o contraseña incorrectos."
                )
            }
            return null
        }

        sesion.guardar(encontrado.id)
        _usuario.value = encontrado
        return encontrado.perfil
    }

    /**
     * Entra directo con la credencial de ejemplo de un rol: para la defensa y las pruebas, sin
     * tipear. No se salta ninguna validacion, solo escribe por vos contra la misma base.
     */
    fun entrarComo(perfil: PerfilUsuario): PerfilUsuario? {
        val credencial = CREDENCIALES_DE_EJEMPLO[perfil] ?: return null
        _uiState.update {
            it.copy(
                rut = credencial.first,
                clave = credencial.second,
                errorRut = null,
                errorClave = null,
                errorGeneral = null
            )
        }
        return entrar()
    }

    fun cerrarSesion() {
        sesion.borrar()
        _usuario.value = null
        _uiState.value = LoginUiState()
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }

    private companion object {
        /** Clave de los colaboradores de ejemplo del prototipo. En produccion no existe. */
        const val CLAVE_DE_EJEMPLO = "rincon2026"

    /** Daniela no cambio su clave: sigue con la del anexo, y su boton entra con esa. */
    const val CLAVE_DE_LA_SUPERVISORA = "rincon2026"

        /** Credenciales de los dos roles de la entrega, para entrar sin tipear. */
        val CREDENCIALES_DE_EJEMPLO = mapOf(
            PerfilUsuario.OBSERVADOR to ("19.876.345-4" to CLAVE_DE_EJEMPLO),
            PerfilUsuario.SUPERVISOR to ("17.654.320-5" to CLAVE_DE_LA_SUPERVISORA)
        )
    }
}
