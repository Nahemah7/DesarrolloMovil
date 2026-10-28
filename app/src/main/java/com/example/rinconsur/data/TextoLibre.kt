package com.example.rinconsur.data

const val TEXTO_MINIMO = 10
const val TEXTO_MAXIMO = 200

fun mensajeDeTextoLibre(texto: String, vacio: String): String? {
    val largo = texto.trim().length
    return when {
        largo == 0 -> vacio
        largo < TEXTO_MINIMO -> "Un poco más de detalle (mínimo " + TEXTO_MINIMO + " caracteres)"
        largo > TEXTO_MAXIMO -> "Máximo " + TEXTO_MAXIMO + " caracteres"
        else -> null
    }
}