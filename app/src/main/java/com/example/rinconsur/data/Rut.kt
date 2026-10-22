package com.example.rinconsur.data

/**
 * RUT chileno: formato y digito verificador (modulo 11).
 *
 * Es logica pura, sin Android: se puede probar sola y la usa el ViewModel del login. Da lo mismo
 * si la persona escribe con puntos, con guion o todo junto: se normaliza antes de comparar.
 * Se valida el digito verificador, no solo que "tenga numeros", porque un RUT mal escrito es un
 * colaborador que no puede entrar a trabajar.
 */
object Rut {

    /** Deja solo numeros y el digito verificador, en mayuscula: "19.876.345-4" -> "198763454". */
    fun limpiar(valor: String): String =
        valor.trim().replace(".", "").replace("-", "").replace(" ", "").uppercase()

    /** Cuerpo y digito: "198763454" -> ("19876345", '4'). */
    private fun partes(limpio: String): Pair<String, Char>? {
        if (limpio.length < 2) return null
        val cuerpo = limpio.dropLast(1)
        if (!cuerpo.all { it.isDigit() }) return null
        return cuerpo to limpio.last()
    }

    /** El digito que le corresponde al cuerpo, segun el modulo 11 (0..9 y K). */
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

    /** Valida un RUT completo. Devuelve null si esta bien, o el mensaje para mostrar. */
    fun validar(valor: String): String? {
        val limpio = limpiar(valor)
        if (limpio.isEmpty()) return "Escribe tu RUT"
        if (limpio.length < 8 || limpio.length > 9) return "El RUT debe tener 8 o 9 caracteres"
        val (cuerpo, digito) = partes(limpio) ?: return "Revisa el RUT: solo números y el dígito verificador"
        return if (digitoVerificador(cuerpo) == digito) null else "El digito verificador no corresponde"
    }

    /** Como se muestra: "19.876.345-4". */
    fun formatear(valor: String): String {
        val limpio = limpiar(valor)
        if (limpio.length < 2) return valor
        val cuerpo = limpio.dropLast(1).reversed().chunked(3).joinToString(".").reversed()
        return cuerpo + "-" + limpio.last()
    }
}
