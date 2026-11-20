package com.example.rinconsur.data.remoto

import android.content.Context

// Guarda la última dirección con la que se conectó: quien prueba desde el teléfono no tiene que
// escribir la IP del PC cada vez que abre la app.
class DireccionServidor(contexto: Context) {

    private val preferencias =
        contexto.getSharedPreferences("rinconsur_servidor", Context.MODE_PRIVATE)

    fun leer(): String = preferencias.getString(CLAVE, null) ?: BuildConfigApi.base

    fun guardar(direccion: String) {
        preferencias.edit().putString(CLAVE, ClienteRemoto.normalizar(direccion)).apply()
    }

    private companion object {
        const val CLAVE = "direccion"
    }
}

// BuildConfig del módulo: se aísla acá para que el resto del código no dependa del paquete generado.
internal object BuildConfigApi {
    val base: String = com.example.rinconsur.BuildConfig.API_BASE
}
