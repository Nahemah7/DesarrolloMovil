package com.example.rinconsur.model

import com.example.rinconsur.data.problemasDeClave

data class PrimerAccesoUiState(
    val clave: String = "",
    val repetir: String = "",
    val catalogo: List<PreguntaSeguridad> = emptyList(),
    val idPregunta: Long? = null,
    val respuesta: String = "",
    val errorClave: String? = null,
    val errorRepetir: String? = null,
    val errorPregunta: String? = null,
    val errorRespuesta: String? = null,
    val guardando: Boolean = false
) {
    val claveValida: Boolean get() = clave.none { it.isWhitespace() } && problemasDeClave(clave).isEmpty()

    val preguntaElegida: PreguntaSeguridad? get() = catalogo.firstOrNull { it.id == idPregunta }

    val puedeGuardar: Boolean
        get() = !guardando &&
            claveValida &&
            clave == repetir &&
            preguntaElegida != null &&
            respuesta.trim().length >= 3

    fun validar(): PrimerAccesoUiState = copy(
        errorClave = if (clave.isBlank()) "Escribe tu nueva contraseña."
        else if (!claveValida) "Falta en la contraseña: " + problemasDeClave(clave).joinToString(", ") + "." else null,
        errorRepetir = if (repetir.isBlank()) "Repite la contraseña."
        else if (clave != repetir) "Las dos contraseñas no coinciden." else null,
        errorPregunta = if (preguntaElegida == null) "Elige tu pregunta de seguridad." else null,
        errorRespuesta = if (respuesta.trim().length < 3) "Escribe la respuesta." else null
    )
}
