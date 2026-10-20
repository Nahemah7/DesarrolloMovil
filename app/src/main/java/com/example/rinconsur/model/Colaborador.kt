package com.example.rinconsur.model

/** Persona que usa la app. La clave nunca se guarda en texto plano: solo su hash. */
data class Colaborador(
    val id: Long,
    val rut: String,
    val nombre: String,
    val cargo: String,
    val perfil: PerfilUsuario,
    val activo: Boolean,
    val passwordHash: String
)
