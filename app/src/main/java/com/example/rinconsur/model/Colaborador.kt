package com.example.rinconsur.model

data class Colaborador(
    val nombre: String,
    val rut: String,
    val cargo: String,
    val perfil: PerfilUsuario,
    val activo: Boolean
)
