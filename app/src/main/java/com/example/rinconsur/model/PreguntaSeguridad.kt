package com.example.rinconsur.model

/**
 * Pregunta de seguridad del catalogo, para recuperar la clave sin correo.
 * Cada colaborador elige dos y las responde en su perfil; las respuestas se guardan hasheadas.
 */
data class PreguntaSeguridad(
    val id: Long,
    val texto: String
)
