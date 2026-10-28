package com.example.rinconsur.model

enum class PerfilUsuario(val codigo: String, val etiqueta: String, val puedeEntrar: Boolean) {
    JEFE_TIENDA("P-01", "Jefa de tienda", false),
    SUBGERENTE("P-02", "Subgerente comercial", false),
    SUPERVISOR("P-03", "Supervisor de piso", true),
    OBSERVADOR("P-04", "Colaborador observador", true);

    companion object {
        fun porCodigo(codigo: String): PerfilUsuario? = entries.firstOrNull { it.codigo == codigo }
    }
}
