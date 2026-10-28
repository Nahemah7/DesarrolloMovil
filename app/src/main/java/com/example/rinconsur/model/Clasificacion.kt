package com.example.rinconsur.model

enum class Clasificacion(val etiqueta: String, val idClasificacion: Long) {
    TRANSITO("Tránsito", 1),
    VISITA("Visita", 2),
    NO_DETERMINADO("No determinado", 3);

    companion object {
        fun porNombre(nombre: String): Clasificacion =
            entries.firstOrNull { it.etiqueta.equals(nombre, ignoreCase = true) } ?: NO_DETERMINADO

        fun porId(id: Long): Clasificacion =
            entries.firstOrNull { it.idClasificacion == id } ?: NO_DETERMINADO
    }
}
