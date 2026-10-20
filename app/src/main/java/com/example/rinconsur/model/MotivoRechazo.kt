package com.example.rinconsur.model

/**
 * Motivos para rechazar una sesion (Anexo 8). El supervisor los marca en casillas, asi no tiene
 * que redactar cada vez; "Otro motivo" activa la casilla de texto. Se guarda como texto en la sesion.
 */
enum class MotivoRechazo(val etiqueta: String) {
    MUESTRA_INSUFICIENTE("Muestra insuficiente (menos de 30 registros)"),
    ERROR_CLASIFICACION("Errores de clasificación"),
    INTERRUMPIDA("Sesión interrumpida"),
    FUERA_DE_HORARIO("Fuera del horario programado"),
    OTRO("Otro motivo")
}
