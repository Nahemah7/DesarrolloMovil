package com.example.rinconsur.data

import kotlin.random.Random

// Código que el administrador entrega a la persona nueva. Se escribe a mano, así que
// no lleva caracteres que se confunden entre sí (0 y O, 1 e I, 5 y S).
object CodigoTemporal {

    private const val ALFABETO = "ACDEFGHJKLMNPQRTUVWXY2346789"
    private const val LARGO = 8

    fun generar(): String {
        val codigo = StringBuilder()
        repeat(LARGO) { codigo.append(ALFABETO[Random.nextInt(ALFABETO.length)]) }
        // Se muestra en dos grupos para dictarlo sin equivocarse.
        return codigo.substring(0, 4) + "-" + codigo.substring(4)
    }

    fun valido(codigo: String): Boolean =
        codigo.length == LARGO + 1 && codigo[4] == '-' &&
            codigo.filter { it != '-' }.all { it in ALFABETO }
}
