package com.example.rinconsur.model

data class Colaborador(
    val id: Long,
    val rut: String,
    val nombre: String,
    val apellido: String = "",
    val cargo: String,
    val perfil: PerfilUsuario,
    val activo: Boolean,
    val passwordHash: String,
    val debeCambiarClave: Boolean = false,
    val codigoVence: String? = null
) {
    val codigoVencido: Boolean
        get() = debeCambiarClave && codigoVence != null &&
            codigoVence.substringBefore(' ') < java.time.LocalDate.now().toString()
}
