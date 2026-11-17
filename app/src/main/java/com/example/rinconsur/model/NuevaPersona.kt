package com.example.rinconsur.model

import com.example.rinconsur.data.Rut

// Lo que el administrador completa para crear una cuenta. El cargo sale del perfil elegido
// y el código temporal lo genera la app al guardar: no se piden dos veces.
data class NuevaPersona(
    val rut: String = "",
    val nombres: String = "",
    val apellidoPaterno: String = "",
    val apellidoMaterno: String = "",
    val correo: String = "",
    val telefono: String = "",
    val rol: PerfilUsuario? = null
) {
    val errores: List<String>
        get() = buildList {
            if (Rut.validar(rut) != null) add("Revisa el RUT: no es válido.")
            if (nombres.trim().length < 2) add("Escribe el nombre.")
            if (apellidoPaterno.trim().length < 2) add("Escribe el apellido.")
            if (!correoValido(correo)) add("Revisa el correo.")
            if (telefono.isNotBlank() && telefono.count { it.isDigit() } < 8) add("Revisa el teléfono.")
            if (rol == null) add("Elige el perfil.")
        }

    val puedeCrear: Boolean get() = errores.isEmpty()

    private fun correoValido(valor: String): Boolean {
        val texto = valor.trim()
        val arroba = texto.indexOf('@')
        return arroba > 0 && texto.substring(arroba).contains('.') && !texto.contains(' ')
    }
}
