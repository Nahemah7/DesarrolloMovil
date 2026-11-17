package com.example.rinconsur.model

enum class PerfilUsuario(val codigo: String, val etiqueta: String, val puedeEntrar: Boolean) {
    JEFE_TIENDA("P-01", "Jefa de tienda", true),
    SUBGERENTE("P-02", "Subgerente comercial", true),
    SUPERVISOR("P-03", "Supervisor de piso", true),
    OBSERVADOR("P-04", "Colaborador observador", true),
    ADMINISTRADOR("P-05", "Administrador", true);

    companion object {
        fun porCodigo(codigo: String): PerfilUsuario? = entries.firstOrNull { it.codigo == codigo }
    }
}
