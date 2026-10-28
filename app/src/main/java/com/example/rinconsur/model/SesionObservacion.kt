package com.example.rinconsur.model

data class SesionObservacion(
    val id: Long,
    val codigo: String,
    val idAcceso: Long,
    val accesoCodigo: String,
    val fecha: String,
    val horaIni: String,
    val horaFin: String,
    val idObservador: Long,
    val observadorRut: String,
    val estado: EstadoSesion,
    val idValidaORechaza: Long? = null,
    val motivoRechazo: String? = null
)
