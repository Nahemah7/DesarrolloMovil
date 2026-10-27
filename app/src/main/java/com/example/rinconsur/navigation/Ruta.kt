package com.example.rinconsur.navigation

/**
 * Rutas de la app. El perfil que entra decide el recorrido: el observador registra y cierra su
 * sesion de conteo; el supervisor programa, valida y mira los indicadores.
 *
 * Por ahora estan las pantallas de entrada y las del observador. Las del supervisor se agregan
 * cuando se construyan.
 */
sealed class Ruta(val ruta: String) {
    // Entrada
    data object Login : Ruta("login")

    // Supervisor de piso
    data object Sesiones : Ruta("sesiones")
    data object Programar : Ruta("programar")
    data object Rechazar : Ruta("rechazar/{idSesion}")
    data object Evento : Ruta("evento")

    // Colaborador observador
    data object MiSesion : Ruta("mi_sesion")
    data object Registro : Ruta("registro")
    data object Plantilla : Ruta("plantilla/{numero}")
    data object Cierre : Ruta("cierre")
    data object SinSenal : Ruta("sin_senal")

    // Comunes
    data object Indicadores : Ruta("indicadores")
    data object Perfil : Ruta("perfil")
}

/** Ruta de la hoja de una plantilla de prerellenado, con su numero. */
fun rutaDePlantilla(numero: Int): String = "plantilla/" + numero

/** Ruta del formulario de rechazo, con el id de la sesion que se rechaza. */
fun rutaDeRechazar(idSesion: Long): String = "rechazar/" + idSesion