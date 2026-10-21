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
    /** Interina, para el supervisor, hasta que existan sus pantallas de conteo. */
    data object Entrada : Ruta("entrada")

    // Supervisor de piso
    data object Programar : Ruta("programar")

    // Colaborador observador
    data object MiSesion : Ruta("mi_sesion")
    data object Registro : Ruta("registro")
    data object Plantilla : Ruta("plantilla")
    data object Cierre : Ruta("cierre")

    // Comunes
    data object Perfil : Ruta("perfil")
}
