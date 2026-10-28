package com.example.rinconsur.model

data class EventoDia(
    val id: Long = 0,
    val codigo: String,
    val fechaInicio: String,
    val fechaFin: String,
    val tipo: TipoEvento,
    val idAcceso: Long? = null,
    val descripcion: String,
    val idUsuarioRegistra: Long = 0,
    val foto: ByteArray? = null,
    val nombreRegistra: String = "",
    val anulado: Boolean = false,
    val motivoAnulacion: String? = null
) {
    val tieneFoto: Boolean get() = foto != null && foto.isNotEmpty()
}
