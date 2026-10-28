package com.example.rinconsur.model

enum class RangoEtario(val etiqueta: String, val idRango: Long) {
    MENOR_18("Menor de 18 años", 1),
    DE_18_A_25("De 18 a 25 años", 2),
    DE_26_A_40("De 26 a 40 años", 3),
    DE_41_A_60("De 41 a 60 años", 4),
    DE_61_O_MAS("61 años o más", 5);

    companion object {
        fun porId(id: Long): RangoEtario = entries.firstOrNull { it.idRango == id } ?: DE_26_A_40
    }
}
