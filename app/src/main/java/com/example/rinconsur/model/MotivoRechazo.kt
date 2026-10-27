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
/**
 * El texto que se guarda en la sesion rechazada: las etiquetas elegidas, y el detalle escrito si
 * entre ellas esta "Otro motivo". Vive aca, y no en el ViewModel, para poder medir su largo en una
 * prueba sin emulador.
 */
fun textoDeRechazo(motivos: Set<MotivoRechazo>, detalle: String): String =
    motivos.joinToString("; ") {
        if (it == MotivoRechazo.OTRO) it.etiqueta + ": " + detalle.trim() else it.etiqueta
    }