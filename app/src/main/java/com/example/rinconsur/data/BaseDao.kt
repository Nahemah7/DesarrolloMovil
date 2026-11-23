package com.example.rinconsur.data

import android.database.Cursor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal fun ahora(): String {
    val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
    formato.timeZone = java.util.TimeZone.getTimeZone("America/Santiago")
    return formato.format(java.util.Date())
}

internal fun Cursor.texto(columna: String): String = getString(getColumnIndexOrThrow(columna)) ?: ""

internal fun Cursor.textoOpcional(columna: String): String? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getString(i)
}

internal fun Cursor.entero(columna: String): Int = getInt(getColumnIndexOrThrow(columna))

internal fun Cursor.long(columna: String): Long = getLong(getColumnIndexOrThrow(columna))

internal fun Cursor.longOpcional(columna: String): Long? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getLong(i)
}

internal fun Cursor.blobOpcional(columna: String): ByteArray? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getBlob(i)
}

internal fun Cursor.decimalOpcional(columna: String): Double? {
    val i = getColumnIndexOrThrow(columna)
    return if (isNull(i)) null else getDouble(i)
}
