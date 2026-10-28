package com.example.rinconsur.model

data class Registro(
    val id: Long,
    val idSesion: Long,
    val codigoSesion: String,
    val fechaHora: String,
    val hora: String,
    val clasificacion: Clasificacion,
    val rangoEtario: RangoEtario,
    val compania: Compania,
    val comprasOtraTienda: Boolean,
    val seDirige: Long? = null,
    val anulado: Boolean = false,
    val idUsuarioAnula: Long? = null,
    val fechaAnulacion: String? = null,
    val motivoAnulacion: String? = null
)
