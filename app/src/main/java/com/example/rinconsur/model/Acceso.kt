package com.example.rinconsur.model

data class Acceso(
    val id: Long,
    val codigo: String,
    val nombre: String,
    val ubicacion: String,
    val observacion: String
) {
    val etiquetaCorta: String get() = codigo + " " + nombre
}
