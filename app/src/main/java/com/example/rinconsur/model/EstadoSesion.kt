package com.example.rinconsur.model

enum class EstadoSesion(val etiqueta: String, val idEstado: Long) {
    PROGRAMADA("Programada", 1),
    EN_CURSO("En curso", 2),
    FINALIZADA("Finalizada", 3),
    VALIDADA("Validada", 4),
    RECHAZADA("Rechazada", 5),
    CANCELADA("Cancelada", 6);

    val esperaRevision: Boolean get() = this == FINALIZADA

    companion object {
        fun porNombre(nombre: String): EstadoSesion =
            entries.firstOrNull { it.etiqueta.equals(nombre, ignoreCase = true) } ?: PROGRAMADA

        fun porId(id: Long): EstadoSesion = entries.firstOrNull { it.idEstado == id } ?: PROGRAMADA
    }
}
