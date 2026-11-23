package com.example.rinconsur.data

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

object FechaTienda {

    const val ZONA = "America/Santiago"

    @Volatile
    var desfaseMilisegundos: Long = 0

    fun zona(): TimeZone = TimeZone.getTimeZone(ZONA)

    fun momento(): java.util.Date = java.util.Date(System.currentTimeMillis() + desfaseMilisegundos)

    fun hoy(): String = formato("yyyy-MM-dd").format(momento())

    fun ahora(): String = formato("yyyy-MM-dd HH:mm:ss").format(momento())

    fun hora(): String = formato("HH:mm:ss").format(momento())

    fun formato(patron: String): SimpleDateFormat {
        val formato = SimpleDateFormat(patron, Locale.US)
        formato.timeZone = zona()
        return formato
    }
}
