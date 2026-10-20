package com.example.rinconsur.model

/** Con quien viene la persona (catalogo `tipo_compania`). */
enum class Compania(val etiqueta: String, val idTipo: Long) {
    SOLO("Solo", 1),
    PAREJA("Pareja", 2),
    CON_NINOS("Con niños", 3),
    GRUPO_3_O_MAS("En grupo de 3 o más", 4);

    companion object {
        fun porId(id: Long): Compania = entries.firstOrNull { it.idTipo == id } ?: SOLO
    }
}
