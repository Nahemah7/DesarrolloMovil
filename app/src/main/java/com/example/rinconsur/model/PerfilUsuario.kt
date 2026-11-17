package com.example.rinconsur.model

// Cada perfil trae el cargo en sus dos formas y el nombre del grupo para listados,
// así el administrador no tiene que escribir el cargo a mano ni queda "Jefa" para un hombre.
enum class PerfilUsuario(
    val codigo: String,
    val femenino: String,
    val masculino: String,
    val grupo: String,
    val puedeEntrar: Boolean
) {
    JEFE_TIENDA("P-01", "Jefa de tienda", "Jefe de tienda", "Jefatura de tienda", true),
    SUBGERENTE("P-02", "Subgerenta comercial", "Subgerente comercial", "Subgerencia comercial", true),
    SUPERVISOR("P-03", "Supervisora de piso", "Supervisor de piso", "Supervisión de piso", true),
    OBSERVADOR("P-04", "Colaboradora observadora", "Colaborador observador", "Colaboradores observadores", true),
    ADMINISTRADOR("P-05", "Administradora", "Administrador", "Administración", true);

    fun etiqueta(genero: Genero): String =
        if (genero == Genero.FEMENINO) femenino else masculino

    // Para cuando todavía no se eligió el género en el formulario.
    val etiqueta: String get() = masculino

    companion object {
        fun porCodigo(codigo: String): PerfilUsuario? = entries.firstOrNull { it.codigo == codigo }
    }
}
