package com.example.rinconsur.model

/**
 * De donde sale el transito de un bloque (RN-14):
 * medido (hay sesion), estimado (se calcula con el promedio) o sin informacion (no hay con que).
 */
enum class OrigenTransito(val etiqueta: String) {
    MEDIDO("medido"),
    ESTIMADO("estimado"),
    SIN_INFORMACION("sin informacion");

    companion object {
        fun porNombre(nombre: String): OrigenTransito =
            entries.firstOrNull { it.etiqueta.equals(nombre, ignoreCase = true) } ?: SIN_INFORMACION
    }
}
