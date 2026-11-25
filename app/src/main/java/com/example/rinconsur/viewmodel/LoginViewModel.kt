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
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import kotlinx.coroutines.launch
import com.example.rinconsur.model.LoginUiState
import com.example.rinconsur.model.Genero
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.Colaborador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

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

        val rutFormateado = Rut.formatear(rutEscrito)
        val encontrado = repositorio.colaboradorPorRut(rutFormateado)
            ?: repositorio.colaboradorPorRut(rutEscrito.trim())
        val claveCorrecta = encontrado != null &&
            Clave.hash(rutFormateado, clave) == encontrado.passwordHash
        if (encontrado == null || !encontrado.activo || !claveCorrecta) {
            _uiState.update {
                it.copy(
                    errorRut = null,
                    errorClave = null,
                    errorGeneral = CREDENCIALES
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
        pedirToken(rutFormateado, clave, encontrado.id)
        return encontrado.perfil
    }

    fun entrar(onListo: (PerfilUsuario?, Boolean) -> Unit) {
        val perfilLocal = entrar()
        if (perfilLocal != null) {
            onListo(perfilLocal, _usuario.value?.debeCambiarClave == true)
            return
        }
        if (_uiState.value.errorGeneral == CREDENCIALES) {
            entrarRemoto(Rut.formatear(_uiState.value.rut), _uiState.value.clave, onListo)
        } else {
            onListo(null, false)
        }
    }

    private fun entrarRemoto(rut: String, clave: String, onListo: (PerfilUsuario?, Boolean) -> Unit) {
        if (_uiState.value.verificando) return
        _uiState.update {
            it.copy(verificando = true, errorRut = null, errorClave = null, errorGeneral = null)
        }

        TrabajoDeFondo.alcance.launch {
            var mensaje: String? = null
            var perfil: PerfilUsuario? = null
            var debeCambiar = false
            var idLocal: Long? = null
            var rolRemoto: String? = null

            try {
                val respuesta = ClienteRemoto(direccion.leer()).autenticacion.entrar(PeticionLogin(rut, clave))
                val cuerpo = respuesta.body()
                val usuarioRemoto = cuerpo?.usuario
                val t = cuerpo?.token

                if (respuesta.isSuccessful && usuarioRemoto != null && !t.isNullOrBlank()) {
                    val rol = PerfilUsuario.porCodigo(usuarioRemoto.rol.orEmpty())
                    if (rol == null) {
                        mensaje = "El perfil de la cuenta no es válido."
                    } else {
                        val completo = usuarioRemoto.nombre.orEmpty().trim()
                        val corte = completo.lastIndexOf(' ')
                        val nombres = if (corte > 0) completo.substring(0, corte) else completo
                        val apellido = if (corte > 0) completo.substring(corte + 1) else ""

                        val id = repositorio.guardarUsuarioDelServidor(
                            rut = usuarioRemoto.rut ?: rut,
                            nombres = nombres,
                            apellidoPaterno = apellido,
                            apellidoMaterno = null,
                            genero = Genero.porCodigo(usuarioRemoto.genero),
                            cargo = usuarioRemoto.cargo.orEmpty(),
                            correo = usuarioRemoto.correo.orEmpty(),
                            telefono = usuarioRemoto.telefono,
                            perfil = rol,
                            clave = clave,
                            debeCambiarClave = usuarioRemoto.debeCambiarClave == true,
                            codigoVence = null
                        )
                        if (id == null) {
                            mensaje = "La cuenta existe en el servidor, pero no se pudo guardar en este teléfono."
                        } else {
                            idLocal = id
                            rolRemoto = rol.codigo
                            perfil = rol
                            debeCambiar = usuarioRemoto.debeCambiarClave == true
                            token.guardar(t, id, rol.codigo)
                        }
                    }
                } else {
                    mensaje = when (respuesta.code()) {
                        401, 403 -> "RUT o contraseña incorrectos."
                        else -> "El servidor respondió " + respuesta.code() + ". Intenta de nuevo en un momento."
                    }
                }
            } catch (e: Exception) {
                mensaje = "Sin conexión con el servidor: revisa la dirección del servidor y vuelve a intentar."
            }

            withContext(Dispatchers.Main) {
                val id = idLocal
                if (id != null) {
                    sesion.guardar(id)
                    _usuario.value = repositorio.colaboradorPorId(id)
                    if (rolRemoto != null && token.leer() != null) {
                        _uiState.update { it.copy(verificando = false, errorGeneral = null) }
                    }
                }
                _uiState.update { it.copy(verificando = false, errorGeneral = mensaje) }
                onListo(perfil, debeCambiar)
            }
        }
    }

    private fun pedirToken(rut: String, clave: String, idUsuario: Long) {

        TrabajoDeFondo.alcance.launch {
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

    private companion object {
        const val CREDENCIALES = "RUT o contraseña incorrectos."
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
