package com.example.rinconsur.model

enum class TipoPeriodo(val etiqueta: String, val tipoDiaBd: String?) {
    HOY("Hoy", null),
    HABILES("Hábiles", "Habil"),
    FIN_DE_SEMANA("Fin de semana", "Fin de semana")
}
