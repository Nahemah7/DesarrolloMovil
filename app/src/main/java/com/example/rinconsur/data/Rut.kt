package com.example.rinconsur.data

object Rut {

    fun limpiar(valor: String): String =
        valor.trim().replace(".", "").replace("-", "").replace(" ", "").uppercase()

    private fun partes(limpio: String): Pair<String, Char>? {
        if (limpio.length < 2) return null
        val cuerpo = limpio.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return null
        return cuerpo to limpio.last()
    }

    fun digitoVerificador(cuerpo: String): Char {
        var suma = 0
        var factor = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * factor
            factor = if (factor == 7) 2 else factor + 1
        }
        return when (val resto = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resto.digitToChar()
        }
    }

    fun validar(valor: String): String? {
        val limpio = limpiar(valor)
        if (limpio.isEmpty()) return "Escribe tu RUT"
        if (limpio.length < 8 || limpio.length > 9) return "El RUT debe tener 8 o 9 caracteres"
        val (cuerpo, digito) = partes(limpio) ?: return "Revisa el RUT: solo números y el dígito verificador"
        return if (digitoVerificador(cuerpo) == digito) null else "El digito verificador no corresponde"
    }

    fun formatear(valor: String): String {
        val limpio = limpiar(valor)
        if (limpio.length < 2) return valor
        val cuerpo = limpio.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return cuerpo + "-" + limpio.last()
    }
}
