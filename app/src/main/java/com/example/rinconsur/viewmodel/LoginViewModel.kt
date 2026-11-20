package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.Clave
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.Rut
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionLogin
import com.example.rinconsur.data.remoto.TokenPreferencias
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.Colaborador
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val sesion = SesionPreferencias(application)
    private val token = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _usuario = MutableStateFlow<Colaborador?>(null)
    val usuario: StateFlow<Colaborador?> = _usuario.asStateFlow()

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

    fun entrar(): PerfilUsuario? {
        val rutEscrito = _uiState.value.rut
        val clave = _uiState.value.clave

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
        if (encontrado.codigoVencido) {
            _uiState.update {
                it.copy(
                    errorRut = null,
                    errorClave = null,
                    errorGeneral = "El código temporal venció. Pídele uno nuevo al administrador."
                )
            }
            return null
        }

        sesion.guardar(encontrado.id)
        _usuario.value = encontrado
        pedirToken(Rut.formatear(rutEscrito), clave, encontrado.id)
        return encontrado.perfil
    }

    // El login se valida en el teléfono, que es lo que permite trabajar sin red. En paralelo se
    // pide el token al gateway: si el servidor no está, la app sigue funcionando con lo local.
    private fun pedirToken(rut: String, clave: String, idUsuario: Long) {
        viewModelScope.launch {
            runCatching {
                val cliente = ClienteRemoto(direccion.leer())
                val respuesta = cliente.autenticacion.entrar(PeticionLogin(rut, clave))
                val cuerpo = respuesta.body()
                val t = cuerpo?.token
                if (respuesta.isSuccessful && !t.isNullOrBlank()) {
                    token.guardar(t, idUsuario, cuerpo?.usuario?.rol.orEmpty())
                }
            }.onFailure { token.borrar() }
        }
    }

    fun cerrarSesion() {
        sesion.borrar()
        token.borrar()
        _usuario.value = null
        _uiState.value = LoginUiState()
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
