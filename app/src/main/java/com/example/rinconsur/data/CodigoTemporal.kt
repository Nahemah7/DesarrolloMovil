package com.example.rinconsur.data

import kotlin.random.Random

object CodigoTemporal {

    private const val ALFABETO = "ACDEFGHJKLMNPQRTUVWXY2346789"
    private const val LARGO = 8

    fun generar(): String {
        val codigo = StringBuilder()
        repeat(LARGO) { codigo.append(ALFABETO[Random.nextInt(ALFABETO.length)]) }

        return codigo.substring(0, 4) + "-" + codigo.substring(4)
    }

    fun valido(codigo: String): Boolean =
        codigo.length == LARGO + 1 && codigo[4] == '-' &&
            codigo.filter { it != '-' }.all { it in ALFABETO }
}
