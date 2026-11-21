package com.example.rinconsur.data.remoto

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// Tareas cortas que no deben morir con la pantalla que las lanzó: pedir el token al iniciar
// sesión, por ejemplo, ocurre justo cuando el ViewModel del login se destruye al navegar.
object TrabajoDeFondo {
    val alcance = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
