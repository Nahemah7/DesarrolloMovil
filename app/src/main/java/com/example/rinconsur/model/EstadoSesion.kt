package com.example.rinconsur.model

/** Estados de una sesion de conteo (catalogo `estado_sesion`). */
enum class EstadoSesion(val etiqueta: String, val idEstado: Long) {
    PROGRAMADA("Programada", 1),
    EN_CURSO("En curso", 2),
    FINALIZADA("Finalizada", 3),
    VALIDADA("Validada", 4),
    RECHAZADA("Rechazada", 5),
    CANCELADA("Cancelada", 6);

    /** Las cerradas por el observador son las que el supervisor tiene que resolver. */
    val esperaRevision: Boolean get() = this == FINALIZADA

    companion object {
        /** Para leer el catalogo de la base por su etiqueta. */
        fun porNombre(nombre: String): EstadoSesion =
            entries.firstOrNull { it.etiqueta.equals(nombre, ignoreCase = true) } ?: PROGRAMADA

        fun porId(id: Long): EstadoSesion = entries.firstOrNull { it.idEstado == id } ?: PROGRAMADA
    }
}
