package com.example.rinconsur.model

// El perfil define el cargo, pero el cargo se escribe según la persona: jefa o jefe.
enum class Genero(val codigo: String, val etiqueta: String) {
    FEMENINO("F", "Femenino"),
    MASCULINO("M", "Masculino");

    companion object {
        fun porCodigo(codigo: String?): Genero =
            entries.firstOrNull { it.codigo == codigo?.trim()?.uppercase() } ?: FEMENINO
    }
}
