package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.CodigoTemporal
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// El administrador crea la persona; el código temporal se genera al guardar y se muestra
// una sola vez, porque en la base queda solo su hash.
class NuevaPersonaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    private val _persona = MutableStateFlow(NuevaPersona())
    val persona: StateFlow<NuevaPersona> = _persona.asStateFlow()

    private val _errores = MutableStateFlow<List<String>>(emptyList())
    val errores: StateFlow<List<String>> = _errores.asStateFlow()

    private val _codigo = MutableStateFlow<String?>(null)
    val codigo: StateFlow<String?> = _codigo.asStateFlow()

    fun onRutCambia(valor: String) = _persona.update { it.copy(rut = valor) }
    fun onNombresCambia(valor: String) = _persona.update { it.copy(nombres = valor) }
    fun onApellidoCambia(valor: String) = _persona.update { it.copy(apellidoPaterno = valor) }
    fun onApellidoMaternoCambia(valor: String) = _persona.update { it.copy(apellidoMaterno = valor) }
    fun onCorreoCambia(valor: String) = _persona.update { it.copy(correo = valor) }
    fun onTelefonoCambia(valor: String) = _persona.update { it.copy(telefono = valor) }
    fun onRolCambia(perfil: PerfilUsuario) = _persona.update { it.copy(rol = perfil) }

    fun crear(): Boolean {
        val datos = _persona.value
        val faltas = datos.errores
        if (faltas.isNotEmpty()) {
            _errores.value = faltas
            return false
        }
        val nuevo = CodigoTemporal.generar()
        val id = repositorio.crearColaborador(datos, nuevo)
        if (id == null) {
            _errores.value = listOf("No se pudo crear la cuenta: revisa si el RUT o el correo ya están en uso.")
            return false
        }
        _errores.value = emptyList()
        _codigo.value = nuevo
        return true
    }

    fun cerrarCodigo() {
        _codigo.value = null
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
