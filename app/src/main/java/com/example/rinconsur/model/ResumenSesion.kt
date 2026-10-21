package com.example.rinconsur.model

/**
 * Lo que el observador ve de su sesion. Solo lleva los tres conteos: el resto se calcula aca para
 * que la formula este escrita una sola vez.
 */
data class ResumenSesion(
    val codigo: String,
    val transito: Int = 0,
    val visita: Int = 0,
    val noDeterminado: Int = 0
) {
    /** Todo lo registrado en la sesion, este clasificado o no. */
    val total: Int get() = transito + visita + noDeterminado

    /** Los sin determinar no entran en la proporcion, asi que no cuentan como clasificados. */
    val clasificados: Int get() = total - noDeterminado

    /**
     * Proporcion de transito, sobre los clasificados: 61 de una sesion de 118 con 5 sin determinar
     * da 61 / (118 - 5) = 54,0 %.
     */
    val pctTransito: Double? get() = porcentajeDe(transito)

    val pctVisita: Double? get() = porcentajeDe(visita)

    /** Con menos de 30 registros clasificados la sesion no se puede validar. */
    val sePuedeValidar: Boolean get() = clasificados >= 30

    /**
     * Cuanto pesa cada grupo sobre todo lo registrado. Es distinto de la proporcion de transito:
     * aca el total incluye los sin determinar (la tabla del cierre) y alla no (la proporcion).
     */
    fun parteDelTotal(cantidad: Int): Double? =
        if (total == 0) null else Math.round(100.0 * cantidad / total * 10.0) / 10.0

    private fun porcentajeDe(cantidad: Int): Double? {
        val base = clasificados
        return if (base == 0) null else Math.round(100.0 * cantidad / base * 10.0) / 10.0
    }
}
