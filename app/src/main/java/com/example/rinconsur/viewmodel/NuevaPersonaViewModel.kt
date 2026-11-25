package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.Rut
import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionUsuarioNueva
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.TrabajoDeFondo
import com.example.rinconsur.model.Genero
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NuevaPersonaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val tokens = TokenPreferencias(application)
    private val direccion = DireccionServidor(application)

    private val _persona = MutableStateFlow(NuevaPersona())
    val persona: StateFlow<NuevaPersona> = _persona.asStateFlow()

    private val _errores = MutableStateFlow<List<String>>(emptyList())
    val errores: StateFlow<List<String>> = _errores.asStateFlow()

    private val _codigo = MutableStateFlow<String?>(null)
    val codigo: StateFlow<String?> = _codigo.asStateFlow()

    private val _guardando = MutableStateFlow(false)
    val guardando: StateFlow<Boolean> = _guardando.asStateFlow()

    fun onRutCambia(valor: String) = _persona.update { it.copy(rut = valor) }
    fun onNombresCambia(valor: String) = _persona.update { it.copy(nombres = valor) }
    fun onApellidoCambia(valor: String) = _persona.update { it.copy(apellidoPaterno = valor) }
    fun onApellidoMaternoCambia(valor: String) = _persona.update { it.copy(apellidoMaterno = valor) }
    fun onCorreoCambia(valor: String) = _persona.update { it.copy(correo = valor) }
    fun onTelefonoCambia(valor: String) = _persona.update { it.copy(telefono = valor) }
    fun onRolCambia(perfil: PerfilUsuario) = _persona.update { it.copy(rol = perfil) }
    fun onGeneroCambia(genero: Genero) = _persona.update { it.copy(genero = genero) }

    fun crear() {
        if (_guardando.value) return
        val datos = _persona.value
        val faltas = datos.errores
        if (faltas.isNotEmpty()) {
            _errores.value = faltas
            return
        }
        val perfil = datos.rol ?: return
        val token = tokens.leer()
        if (token.isNullOrBlank()) {
            _errores.value = listOf(
                "Para crear la cuenta necesitas sesión en el servidor. Revisa la conexión y vuelve a entrar."
            )
            return
        }

        _errores.value = emptyList()
        _guardando.value = true
        TrabajoDeFondo.alcance.launch {
            try {
                val cliente = ClienteRemoto(direccion.leer())
                val respuesta = cliente.usuarios.crear(
                    "Bearer " + token,
                    PeticionUsuarioNueva(
                        rut = Rut.formatear(datos.rut),
                        nombres = datos.nombres.trim(),
                        apellidoPaterno = datos.apellidoPaterno.trim(),
                        apellidoMaterno = datos.apellidoMaterno.trim().ifBlank { null },
                        correo = datos.correo.trim().lowercase(),
                        telefono = datos.telefono.trim().ifBlank { null },
                        genero = datos.genero.codigo,
                        rol = perfil.codigo
                    )
                )
                val creado = respuesta.body()
                val codigoServidor = creado?.codigoTemporal
                val usuarioServidor = creado?.usuario
                if (!respuesta.isSuccessful || codigoServidor.isNullOrBlank() || usuarioServidor == null) {
                    _errores.value = listOf(motivoDe(respuesta.code()))
                    return@launch
                }

                val id = repositorio.guardarUsuarioDelServidor(
                    rut = usuarioServidor.rut ?: Rut.formatear(datos.rut),
                    nombres = datos.nombres.trim(),
                    apellidoPaterno = datos.apellidoPaterno.trim(),
                    apellidoMaterno = datos.apellidoMaterno.trim().ifBlank { null },
                    genero = datos.genero,
                    cargo = usuarioServidor.cargo.orEmpty(),
                    correo = datos.correo.trim().lowercase(),
                    telefono = datos.telefono.trim().ifBlank { null },
                    perfil = perfil,
                    clave = codigoServidor,
                    debeCambiarClave = true,
                    codigoVence = repositorio.vencimientoDeCodigo()
                )
                if (id == null) {
                    _errores.value = listOf(
                        "La cuenta quedó creada en el servidor, pero no se pudo guardar en este teléfono."
                    )
                    return@launch
                }
                _codigo.value = codigoServidor
            } catch (e: Exception) {
                _errores.value = listOf(
                    "No se pudo conectar con el servidor: la cuenta no se creó. Revisa la dirección del servidor y vuelve a intentar."
                )
            } finally {
                _guardando.value = false
            }
        }
    }

    private fun motivoDe(codigo: Int): String = when (codigo) {
        400 -> "El servidor rechazó los datos: revisa el RUT, el correo y el perfil."
        401 -> "La sesión venció: vuelve a entrar con tu cuenta de administración."
        403 -> "Solo el perfil de administración puede crear cuentas."
        409 -> "Ya existe una cuenta con ese RUT o ese correo en el servidor."
        else -> "El servidor respondió " + codigo + ". La cuenta no se creó."
    }

    fun cerrarCodigo() {
        _codigo.value = null
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
