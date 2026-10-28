package com.example.rinconsur.model

enum class OrigenTransito(val etiqueta: String) {
    MEDIDO("medido"),
    ESTIMADO("estimado"),
    SIN_INFORMACION("sin información");

    companion object {
        fun porNombre(nombre: String): OrigenTransito =
            entries.firstOrNull { it.etiqueta.equals(nombre, ignoreCase = true) } ?: SIN_INFORMACION
    }
}
