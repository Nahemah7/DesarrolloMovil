package com.example.rinconsur.navigation

sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object PrimerAcceso : Ruta("primer_acceso")

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
    data object Reporte : Ruta("reporte")
    data object Carga : Ruta("carga")
    data object Perfil : Ruta("perfil")

    data object Colaboradores : Ruta("colaboradores")
    data object NuevaPersona : Ruta("nueva_persona")
    data object SesionDetalle : Ruta("sesion/{idSesion}")
}

fun rutaDePlantilla(numero: Int): String = "plantilla/" + numero

fun rutaDeRechazar(idSesion: Long): String = "rechazar/" + idSesion

fun rutaDeDetalleSesion(idSesion: Long): String = "sesion/" + idSesion
