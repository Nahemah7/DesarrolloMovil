package com.example.rinconsur.data

import java.security.MessageDigest

object Clave {

    fun hash(rut: String, clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((rut + "|" + clave).toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}

fun problemasDeClave(clave: String): List<String> {
    val faltas = mutableListOf<String>()
    if (clave.length < 8) faltas.add("al menos 8 caracteres")
    if (clave.none { it.isUpperCase() }) faltas.add("una mayúscula")
    if (clave.none { it.isLowerCase() }) faltas.add("una minúscula")
    if (clave.none { it.isDigit() }) faltas.add("un número")
    return faltas
}

fun esClaveValida(clave: String): Boolean = problemasDeClave(clave).isEmpty()

fun mensajeDeClave(clave: String): String? {
    val faltas = problemasDeClave(clave)
    return if (faltas.isEmpty()) null else "La clave necesita " + faltas.joinToString(", ") + "."
}
