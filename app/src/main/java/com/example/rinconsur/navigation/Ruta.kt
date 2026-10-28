package com.example.rinconsur.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")

    data object Sesiones : Ruta("sesiones")
    data object Programar : Ruta("programar")
    data object Rechazar : Ruta("rechazar/{idSesion}")
    data object Evento : Ruta("evento")

    data object MiSesion : Ruta("mi_sesion")
    data object Registro : Ruta("registro")
    data object Plantilla : Ruta("plantilla/{numero}")
    data object Cierre : Ruta("cierre")
    data object SinSenal : Ruta("sin_senal")

    data object Indicadores : Ruta("indicadores")
    data object Perfil : Ruta("perfil")
}

fun rutaDePlantilla(numero: Int): String = "plantilla/" + numero

fun rutaDeRechazar(idSesion: Long): String = "rechazar/" + idSesion