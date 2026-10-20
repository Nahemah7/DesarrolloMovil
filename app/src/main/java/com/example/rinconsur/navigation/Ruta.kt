package com.example.rinconsur.navigation

/**
 * Rutas de la app. Por ahora la entrada: el login y la pantalla de sesion iniciada.
 * Las pantallas de conteo (observador y supervisor) se agregan cuando se construyan.
 */
sealed class Ruta(val ruta: String) {
    data object Login : Ruta("login")
    data object Entrada : Ruta("entrada")
}
