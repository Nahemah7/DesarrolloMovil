package com.example.rinconsur.model

/** Los 11 bloques de una hora, de 10:00 a 20:59 (RN-02). */
data class BloqueHorario(
    val id: Long,
    val horaInicio: String,
    val horaFin: String
) {
    /** "13:00" a partir de "13:00:00" */
    val horaInicioCorta: String get() = horaInicio.take(5)

    companion object {
        /** A que bloque pertenece una hora del dia (1 a 11). */
        fun idPorHora(hora: Int): Long = (hora - 9).toLong().coerceIn(1L, 11L)
    }
}
