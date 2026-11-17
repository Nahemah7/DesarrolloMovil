package com.example.rinconsur.model

data class Colaborador(
    val id: Long,
    val rut: String,
    val nombre: String,
    val apellido: String = "",
    val cargo: String = "",
    val perfil: PerfilUsuario = PerfilUsuario.OBSERVADOR,
    val activo: Boolean = true,
    val passwordHash: String = "",
    val genero: Genero = Genero.FEMENINO,
    val correo: String = "",
    val telefono: String? = null,
    val debeCambiarClave: Boolean = false,
    val codigoVence: String? = null
) {
    val codigoVencido: Boolean
        get() = debeCambiarClave && codigoVence != null &&
            codigoVence.substringBefore(' ') < java.time.LocalDate.now().toString()
}
