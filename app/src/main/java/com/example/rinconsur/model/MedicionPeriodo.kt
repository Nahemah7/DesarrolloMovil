package com.example.rinconsur.model

data class MedicionPeriodo(
    val personas: Int = 0,
    val bloques: Int = 0,
    val dias: Int = 0,
    val sesionesValidadas: Int = 0
) {
    val sinMedicion: Boolean get() = personas == 0
}
