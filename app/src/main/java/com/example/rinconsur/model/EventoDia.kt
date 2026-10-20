package com.example.rinconsur.model

/** Evento que explica el comportamiento del dia (Anexo 9.6). */
data class EventoDia(
    val id: Long = 0,
    val codigo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val tipo: TipoEvento,
    val idAcceso: Long? = null,
    val descripcion: String,
    val idUsuarioRegistra: Long = 0,
    /** Foto de evidencia (camara o galeria). Va como bytes en la base; null = sin foto. */
    val foto: ByteArray? = null
) {
    val tieneFoto: Boolean get() = foto != null && foto.isNotEmpty()
}
