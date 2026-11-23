package com.example.rinconsur.model

data class RendimientoSupervisor(
    val idSupervisor: Long = 0,
    val nombre: String = "",
    val programadas: Int = 0,
    val pendientes: Int = 0,
    val validadas: Int = 0,
    val rechazadas: Int = 0
)
