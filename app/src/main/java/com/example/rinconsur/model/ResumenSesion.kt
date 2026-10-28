package com.example.rinconsur.model

data class ResumenSesion(
    val codigo: String,
    val transito: Int = 0,
    val visita: Int = 0,
    val noDeterminado: Int = 0
) {
    val total: Int get() = transito + visita + noDeterminado

    val clasificados: Int get() = total - noDeterminado

    val pctTransito: Double? get() = porcentajeDe(transito)

    val pctVisita: Double? get() = porcentajeDe(visita)

    val sePuedeValidar: Boolean get() = clasificados >= 30

    fun parteDelTotal(cantidad: Int): Double? =
        if (total == 0) null else Math.round(100.0 * cantidad / total * 10.0) / 10.0

    private fun porcentajeDe(cantidad: Int): Double? {
        val base = clasificados
        return if (base == 0) null else Math.round(100.0 * cantidad / base * 10.0) / 10.0
    }
}
