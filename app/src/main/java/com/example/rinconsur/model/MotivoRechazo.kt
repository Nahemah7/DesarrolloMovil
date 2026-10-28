package com.example.rinconsur.model

enum class MotivoRechazo(val etiqueta: String) {
    MUESTRA_INSUFICIENTE("Muestra insuficiente (menos de 30 registros)"),
    ERROR_CLASIFICACION("Errores de clasificación"),
    INTERRUMPIDA("Sesión interrumpida"),
    FUERA_DE_HORARIO("Fuera del horario programado"),
    OTRO("Otro motivo")
}
fun textoDeRechazo(motivos: Set<MotivoRechazo>, detalle: String): String =
    motivos.joinToString("; ") {
        if (it == MotivoRechazo.OTRO) it.etiqueta + ": " + detalle.trim() else it.etiqueta
    }