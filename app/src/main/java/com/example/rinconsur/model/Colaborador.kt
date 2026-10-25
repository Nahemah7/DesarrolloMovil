package com.example.rinconsur.model

/** Persona que usa la app. La clave nunca se guarda en texto plano: solo su hash. */
data class Colaborador(
    val id: Long,
    val rut: String,
    val nombre: String,
    /** Apellido paterno: la lista de sesiones del supervisor muestra nombre y apellido. */
    val apellido: String = "",
    val cargo: String,
    val perfil: PerfilUsuario,
    val activo: Boolean,
    val passwordHash: String
)
