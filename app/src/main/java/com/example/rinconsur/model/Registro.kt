package com.example.rinconsur.model

data class Registro(
    val codigoSesion: String,
    val codigo: String,
    val hora: String,
    val clasificacion: Clasificacion,
    val rangoEtario: RangoEtario,
    val compania: Compania,
    val compraDeOtraTienda: Boolean,
    val seDirige: String?
)
