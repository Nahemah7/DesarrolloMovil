package com.example.rinconsur.data.remoto

import android.content.Context
import java.util.Locale
import java.util.TimeZone

class RelojDelServidor(contexto: Context) {

    private val prefs = contexto.getSharedPreferences("rinconsur_reloj", Context.MODE_PRIVATE)

    fun desfaseMinutos(): Int = prefs.getInt(DESFASE, 0)

    fun aplicar() {
        val guardado = prefs.getLong(DESFASE_MS, 0L)
        com.example.rinconsur.data.FechaTienda.desfaseMilisegundos = guardado
    }

    suspend fun sincronizar(direccion: String): Int {
        val equipo = minutosDelDia(TimeZone.getDefault())
        val tienda = minutosDelDia(TimeZone.getTimeZone(TIENDA))
        val minutos = equipo - tienda
        prefs.edit().putInt(DESFASE, minutos).apply()
        val delServidor = instanteDelServidor(direccion)
        if (delServidor != null) {
            val desfase = delServidor - System.currentTimeMillis()
            prefs.edit().putLong(DESFASE_MS, desfase).apply()
            com.example.rinconsur.data.FechaTienda.desfaseMilisegundos = desfase
        }
        return minutos
    }

    private suspend fun instanteDelServidor(direccion: String): Long? {
        val respuesta = runCatching {
            ClienteRemoto(direccion).autenticacion.preguntas()
        }.getOrNull() ?: return null
        val cabecera = respuesta.headers()["Date"] ?: return null
        return runCatching {
            val formato = java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
            formato.timeZone = TimeZone.getTimeZone("GMT")
            formato.parse(cabecera)?.time
        }.getOrNull()
    }

    private fun minutosDelDia(zona: TimeZone): Int {
        val formato = java.text.SimpleDateFormat("HH:mm", Locale.US)
        formato.timeZone = zona
        val partes = formato.format(java.util.Date()).split(":")
        return partes[0].toInt() * 60 + partes[1].toInt()
    }

    private companion object {
        const val DESFASE = "desfase_minutos"
        const val DESFASE_MS = "desfase_milisegundos"
        const val TIENDA = "America/Santiago"
    }
}
