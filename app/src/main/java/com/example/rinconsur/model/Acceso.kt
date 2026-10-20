package com.example.rinconsur.model

/** Acceso fisico de la tienda (A1 Principal, A2 Lateral, A3 Estacionamiento, A4 Conexion). */
data class Acceso(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val observacion: String
)
