package com.example.rinconsur.model

/**
 * Clasificacion del flujo (catalogo `clasificacion`, 3 estados del anexo).
 * "No determinado" es categoria propia: no es un error del observador.
 */
enum class Clasificacion(val etiqueta: String, val idClasificacion: Long) {
    TRANSITO("Tránsito", 1),
    VISITA("Visita", 2),
    NO_DETERMINADO("No determinado", 3);

    companion object {
        fun porId(id: Long): Clasificacion =
            entries.firstOrNull { it.idClasificacion == id } ?: NO_DETERMINADO
    }
}
