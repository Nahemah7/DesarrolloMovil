package com.example.rinconsur.data.remoto

import android.content.Context
import java.util.Locale
import java.util.TimeZone

class RelojDelServidor(contexto: Context) {

    private val prefs = contexto.getSharedPreferences("rinconsur_reloj", Context.MODE_PRIVATE)

    fun desfaseMinutos(): Int = prefs.getInt(DESFASE, 0)

    fun medir(): Int {
        val equipo = minutosDelDia(TimeZone.getDefault())
        val tienda = minutosDelDia(TimeZone.getTimeZone(TIENDA))
        val minutos = equipo - tienda
        prefs.edit().putInt(DESFASE, minutos).apply()
        return minutos
    }

    private fun minutosDelDia(zona: TimeZone): Int {
        val formato = java.text.SimpleDateFormat("HH:mm", Locale.US)
        formato.timeZone = zona
        val partes = formato.format(java.util.Date()).split(":")
        return partes[0].toInt() * 60 + partes[1].toInt()
    }

    private companion object {
        const val DESFASE = "desfase_minutos"
        const val TIENDA = "America/Santiago"
    }
}
