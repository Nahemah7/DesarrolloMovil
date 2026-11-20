package com.example.rinconsur.model

// Lo que mide el observador, sin depender de los ingresos ni de las transacciones de la tienda:
// cuántas personas registró, cuántos bloques cubrió y en cuántos días trabajó.
data class MedicionPeriodo(
    val personas: Int = 0,
    val bloques: Int = 0,
    val dias: Int = 0,
    val sesionesValidadas: Int = 0
) {
    val sinMedicion: Boolean get() = personas == 0
}
