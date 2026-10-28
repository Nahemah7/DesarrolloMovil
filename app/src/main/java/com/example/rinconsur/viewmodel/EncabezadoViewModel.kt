package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.rinconsur.data.EstadoRed
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PerfilUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class EncabezadoViewModel(application: Application) : AndroidViewModel(application) {

    private val repositorio = FlujoRepository(application)
    private val preferencias = SesionPreferencias(application)
    private val red = EstadoRed(application)

    private val _perfil = MutableStateFlow(PerfilUsuario.OBSERVADOR)
    val perfil: StateFlow<PerfilUsuario> = _perfil.asStateFlow()

    private val _titulo = MutableStateFlow("Rincón Sur")
    val titulo: StateFlow<String> = _titulo.asStateFlow()

    private val _subtitulo = MutableStateFlow("")
    val subtitulo: StateFlow<String> = _subtitulo.asStateFlow()

    private fun usuarioActual(): Colaborador? {
        val id = preferencias.leer()
        return if (id > 0) repositorio.colaboradorPorId(id) else null
    }

    fun actualizar(ruta: String, numeroPlantilla: String? = null, idSesion: String? = null) {
        val usuario = usuarioActual()
        _perfil.value = usuario?.perfil ?: PerfilUsuario.OBSERVADOR
        val quien = usuario?.let { it.nombre + " · " + it.perfil.etiqueta } ?: ""
        val sesion = usuario?.let { repositorio.sesionEnCursoDe(it.id) }
        val acceso = sesion?.let { s ->
            repositorio.accesos().firstOrNull { it.id == s.idAcceso }?.etiquetaCorta
        } ?: ""

        val (titulo, subtitulo) = when {
            ruta.startsWith("mi_sesion") -> "Mi sesión" to quien

            ruta.startsWith("registro") ->
                (sesion?.codigo ?: "Sesión") to
                        listOf("Registrando accesos", acceso).filter { it.isNotEmpty() }.joinToString(" · ")

            ruta.startsWith("plantilla") -> "Plantilla " + (numeroPlantilla ?: "") to "Prerellenado del registro"

            ruta.startsWith("cierre") ->
                (sesion?.codigo ?: "Cerrar sesión") to "Resumen de lo registrado"

            ruta.startsWith("sin_senal") ->
                "Envío de sesiones" to red.medioDeConexion().etiqueta

            ruta.startsWith("sesiones") -> "Sesiones" to "Validar · rechazar"

            ruta.startsWith("indicadores") -> "Indicadores" to repositorio.fechaHoy()

            ruta.startsWith("programar") -> "Programar sesión" to "Nueva sesión de conteo"

            ruta.startsWith("evento") -> "Evento del día" to repositorio.fechaHoy()

            ruta.startsWith("rechazar") ->
                "Rechazar sesión" to (repositorio.sesion(idSesion?.toLongOrNull() ?: 0L)?.codigo ?: "")

            ruta.startsWith("perfil") -> "Mi perfil" to quien

            else -> "Rincón Sur" to "Acceso al sistema"
        }

        _titulo.value = titulo
        _subtitulo.value = subtitulo
    }

    override fun onCleared() {
        repositorio.cerrar()
        super.onCleared()
    }
}