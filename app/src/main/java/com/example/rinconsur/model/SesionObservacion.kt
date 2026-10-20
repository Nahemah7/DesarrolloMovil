package com.example.rinconsur.model

/**
 * Sesion de conteo: 30 a 60 minutos en un acceso, sin traslape (RN-02).
 * El rechazo no borra nada: guarda quien reviso, cuando y por que (RN-11).
 */
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
