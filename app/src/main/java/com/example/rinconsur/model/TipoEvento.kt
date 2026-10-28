package com.example.rinconsur.model

enum class TipoEvento(val etiqueta: String, val idTipo: Long) {
    CLIMA("Clima", 1),
    FERIADO("Feriado", 2),
    EVENTO_MALL("Evento en el mall", 3),
    CIERRE_ACCESO("Cierre de acceso", 4),
    CAMPANA("Campaña comercial", 5),
    OTRO("Otro", 6);
}
