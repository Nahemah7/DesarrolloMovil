package com.example.rinconsur.data

import android.content.Context
import androidx.core.content.edit

/**
 * Quien esta usando el telefono queda guardado aca (SharedPreferences), no en la base:
 * es un dato del equipo, no de la tienda. Al cerrar sesion se borra, porque el
 * telefono se comparte entre turnos y nadie debe quedar adentro.
 */
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
