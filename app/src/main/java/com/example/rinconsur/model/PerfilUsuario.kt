package com.example.rinconsur.model

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

    val etiqueta: String get() = masculino

    companion object {
        fun porCodigo(codigo: String): PerfilUsuario? = entries.firstOrNull { it.codigo == codigo }
    }
}
