package com.example.rinconsur.data

internal fun dividirSentencias(script: String): List<String> {
    val salida = mutableListOf<String>()
    val actual = StringBuilder()
    var enTexto = false
    var i = 0
    while (i < script.length) {
        val c = script[i]
        when {
            c == '\'' -> {
                enTexto = !enTexto
                actual.append(c)
            }
            !enTexto && c == '-' && i + 1 < script.length && script[i + 1] == '-' -> {
                while (i < script.length && script[i] != '\n') i++
                actual.append('\n')
            }
            !enTexto && c == ';' -> {
                if (actual.isNotBlank()) salida.add(actual.toString().trim())
                actual.clear()
            }
            else -> actual.append(c)
        }
        i++
    }
    if (actual.isNotBlank()) salida.add(actual.toString().trim())
    return salida.filter { it.isNotEmpty() }
}
