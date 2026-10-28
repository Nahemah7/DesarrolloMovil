package com.example.rinconsur.data

import android.content.Context
import androidx.core.content.edit

class SesionPreferencias(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    companion object {
        private const val CLAVE_ID_USUARIO = "id_usuario"
        private const val SIN_SESION = -1L
    }

    fun guardar(idColaborador: Long) {
        prefs.edit { putLong(CLAVE_ID_USUARIO, idColaborador) }
    }

    fun leer(): Long = prefs.getLong(CLAVE_ID_USUARIO, SIN_SESION)

    fun haySesion(): Boolean = leer() != SIN_SESION

    fun borrar() {
        prefs.edit { remove(CLAVE_ID_USUARIO) }
    }
}
