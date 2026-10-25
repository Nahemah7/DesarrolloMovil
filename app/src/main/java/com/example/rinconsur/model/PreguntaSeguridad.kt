package com.example.rinconsur.model

/**
 * Pregunta de seguridad del catalogo, para recuperar la clave sin correo.
 * Cada colaborador elige una y la responde en su perfil; la respuesta se guarda hasheada.
 */
data class PreguntaSeguridad(
    val id: Long,
    val texto: String
)
