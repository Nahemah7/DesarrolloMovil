package com.example.rinconsur.data

import java.security.MessageDigest

/**
 * Hash de la contrasena.
 *
 * La clave nunca se guarda ni se compara en texto plano: se guarda SHA-256 de "RUT|clave" y se
 * comparan los dos hashes. Asi, si alguien abre la base, no puede leer ninguna contrasena.
 *
 * El salt es el RUT (unico por persona). En un sistema real seria aleatorio y guardado en su
 * propia columna; aca se usa el RUT para no agregar una columna que el caso no pide.
 *
 * OJO con el formato: el RUT tiene que ir **como se muestra** (19.876.345-4, con puntos y guion),
 * que es como esta en la semilla. Si un lado lo limpia (198763454) y el otro no, los hashes no
 * coinciden nunca y el login falla sin decir por que.
 */
object Clave {

    fun hash(rut: String, clave: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest((rut + "|" + clave).toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte) }
}

/**
 * Requisitos de la clave, decididos para esta app: 8 caracteres como minimo, con mayuscula,
 * minuscula y numero. Devuelve la lista de lo que le falta, vacia si esta bien: la pantalla
 * necesita el detalle para poder decir que falta, no un si/no.
 */
fun problemasDeClave(clave: String): List<String> {
    val faltas = mutableListOf<String>()
    if (clave.length < 8) faltas.add("al menos 8 caracteres")
    if (clave.none { it.isUpperCase() }) faltas.add("una mayúscula")
    if (clave.none { it.isLowerCase() }) faltas.add("una minúscula")
    if (clave.none { it.isDigit() }) faltas.add("un número")
    return faltas
}

/** La clave cumple los requisitos. */
fun esClaveValida(clave: String): Boolean = problemasDeClave(clave).isEmpty()

/** Mensaje para mostrar cuando la clave no cumple, con lo que falta. */
fun mensajeDeClave(clave: String): String? {
    val faltas = problemasDeClave(clave)
    return if (faltas.isEmpty()) null else "La clave necesita " + faltas.joinToString(", ") + "."
}
