package com.example.rinconsur.model

/** Lo que el observador ve al cerrar su sesion (pantalla 05). */
data class ResumenSesion(
    val codigo: String,
    val transito: Int = 0,
    val visita: Int = 0,
    val noDeterminado: Int = 0
) {
    val total: Int get() = transito + visita + noDeterminado

    /** Los "no determinado" no entran en el denominador (RN-13). */
    val pctTransito: Double?
        get() {
            val base = total - noDeterminado
            return if (base == 0) null else redondear(100.0 * transito / base)
        }

    val pctVisita: Double?
        get() { val base = total - noDeterminado; return if (base == 0) null else redondear(100.0 * visita / base) }

    fun porcentajeDe(cantidad: Int): Double? =
        if (total == 0) null else redondear(100.0 * cantidad / total)

    private fun redondear(v: Double): Double = Math.round(v * 10.0) / 10.0
}
