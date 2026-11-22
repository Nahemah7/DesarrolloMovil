package com.example.rinconsur.data.remoto

import android.content.Context

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

internal object BuildConfigApi {
    val base: String = com.example.rinconsur.BuildConfig.API_BASE
}
