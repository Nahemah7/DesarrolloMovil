package com.example.rinconsur.model

data class SesionObservacion(
    val codigo: String,
    val accesoCodigo: String,
    val fecha: String,
    val horaIni: String,
    val horaFin: String,
    val observadorRut: String,
    val estado: EstadoSesion,
    val validaORechazaRut: String?,
    val motivoRechazo: String?
)