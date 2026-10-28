package com.example.rinconsur.model

data class BloqueHorario(
    val id: Long,
    val horaInicio: String,
    val horaFin: String
) {
    val horaInicioCorta: String get() = horaInicio.take(5)

    companion object {
        fun idPorHora(hora: Int): Long = (hora - 9).toLong().coerceIn(1L, 11L)
    }
}
