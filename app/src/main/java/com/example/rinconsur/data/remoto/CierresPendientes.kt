package com.example.rinconsur.data.remoto

import android.content.Context

class CierresPendientes(contexto: Context) {

    private val prefs = contexto.getSharedPreferences("rinconsur_cierres", Context.MODE_PRIVATE)

    fun guardar(idRemoto: Long, hora: String, idUsuario: Long) {
        val actuales = leer().toMutableMap()
        actuales[idRemoto] = hora + "|" + idUsuario
        prefs.edit().putString(CLAVE, escribir(actuales)).apply()
    }

    fun olvidar(idRemoto: Long) {
        val actuales = leer().toMutableMap()
        if (actuales.remove(idRemoto) != null) {
            prefs.edit().putString(CLAVE, escribir(actuales)).apply()
        }
    }

    fun leer(): Map<Long, String> {
        val crudo = prefs.getString(CLAVE, "").orEmpty()
        if (crudo.isBlank()) return emptyMap()
        return crudo.split(";")
            .mapNotNull { trozo ->
                val partes = trozo.split(":")
                val id = partes.getOrNull(0)?.toLongOrNull() ?: return@mapNotNull null
                id to partes.getOrNull(1).orEmpty()
            }
            .toMap()
    }

    fun cantidad(): Int = leer().size

    private fun escribir(mapa: Map<Long, String>): String =
        mapa.entries.joinToString(";") { it.key.toString() + ":" + it.value }

    private companion object {
        const val CLAVE = "cierres"
    }
}
