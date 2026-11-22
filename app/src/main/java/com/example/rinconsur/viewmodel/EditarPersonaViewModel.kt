package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.Genero
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class EditarPersonaViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)

    private var original: Colaborador? = null

    private val _persona = MutableStateFlow(NuevaPersona())
    val persona: StateFlow<NuevaPersona> = _persona.asStateFlow()

    private val _perfil = MutableStateFlow(PerfilUsuario.OBSERVADOR)
    val perfil: StateFlow<PerfilUsuario> = _perfil.asStateFlow()

    private val _activo = MutableStateFlow(true)
    val activo: StateFlow<Boolean> = _activo.asStateFlow()

    private val _rut = MutableStateFlow("")
    val rut: StateFlow<String> = _rut.asStateFlow()

    private val _errores = MutableStateFlow<List<String>>(emptyList())
    val errores: StateFlow<List<String>> = _errores.asStateFlow()

    private val _aviso = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val aviso: SharedFlow<String> = _aviso.asSharedFlow()

    fun cargar(idUsuario: Long) {
        val persona = repositorio.colaboradorPorId(idUsuario) ?: return
        original = persona
        _rut.value = persona.rut
        _perfil.value = persona.perfil
        _activo.value = persona.activo
        _persona.value = NuevaPersona(
            rut = persona.rut,
            nombres = persona.nombre,
            apellidoPaterno = persona.apellido,
            correo = persona.correo,
            telefono = persona.telefono ?: "",
            rol = persona.perfil,
            genero = persona.genero
        )
    }

    fun onNombresCambia(valor: String) = _persona.update { it.copy(nombres = valor) }
    fun onApellidoCambia(valor: String) = _persona.update { it.copy(apellidoPaterno = valor) }
    fun onApellidoMaternoCambia(valor: String) = _persona.update { it.copy(apellidoMaterno = valor) }
    fun onCorreoCambia(valor: String) = _persona.update { it.copy(correo = valor) }
    fun onTelefonoCambia(valor: String) = _persona.update { it.copy(telefono = valor) }
    fun onGeneroCambia(genero: Genero) = _persona.update { it.copy(genero = genero) }
    fun onPerfilCambia(perfil: PerfilUsuario) {
        _perfil.value = perfil
        _persona.update { it.copy(rol = perfil) }
    }
    fun onActivoCambia(valor: Boolean) {
        _activo.value = valor
    }

    fun guardar(): Boolean {
        val persona = original ?: return false
        val datos = _persona.value

        val faltas = datos.errores.filterNot { it.startsWith("Revisa el RUT") }
        if (faltas.isNotEmpty()) {
            _errores.value = faltas
            return false
        }
        val guardado = repositorio.actualizarColaborador(
            idUsuario = persona.id,
            datos = datos.copy(rut = persona.rut),
            perfil = _perfil.value,
            genero = datos.genero,
            activo = _activo.value
        )
        if (!guardado) {
            _errores.value = listOf("No se pudo guardar: revisa si el correo ya está en uso.")
            return false
        }
        _errores.value = emptyList()
        _aviso.tryEmit("Datos de " + datos.nombres + " actualizados.")
        return true
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}
