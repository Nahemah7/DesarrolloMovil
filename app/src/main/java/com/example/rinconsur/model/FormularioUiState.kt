package com.example.rinconsur.model

enum class NivelExperiencia(val etiqueta: String) {
    PRINCIPIANTE("Principiante"),
    INTERMEDIO("Intermedio"),
    AVANZADO("Avanzado")
}

data class FormularioUiState(
    // Valores de los campos
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val nivel: com.example.rinconsur.model.NivelExperiencia = com.example.rinconsur.model.NivelExperiencia.PRINCIPIANTE,
    val recibirNoticias: Boolean = false,
    val aceptaTerminos: Boolean = false,

    // Estado visual
    val contrasenaVisible: Boolean = false,

    // Errores de validación (null = sin error)
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorTerminos: String? = null,

    // Resultado
    val registroExitoso: Boolean = false
)