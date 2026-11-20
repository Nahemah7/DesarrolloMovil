package com.example.rinconsur.data.remoto

import android.content.Context

// El token que devuelve el gateway al iniciar sesión. Se guarda para que las pantallas que ya
// hablan con la API no tengan que volver a pedirlo, y se borra al cerrar sesión.
class TokenPreferencias(contexto: Context) {

    private val preferencias = contexto.getSharedPreferences("rinconsur_token", Context.MODE_PRIVATE)

    fun guardar(token: String, idUsuario: Long, rol: String) {
        preferencias.edit()
            .putString(TOKEN, token)
            .putLong(ID, idUsuario)
            .putString(ROL, rol)
            .apply()
    }

    fun leer(): String? = preferencias.getString(TOKEN, null)

    fun idUsuario(): Long = preferencias.getLong(ID, 0L)

    fun rol(): String? = preferencias.getString(ROL, null)

    fun borrar() {
        preferencias.edit().clear().apply()
    }

    private companion object {
        const val TOKEN = "token"
        const val ID = "id_usuario"
        const val ROL = "rol"
    }
}
