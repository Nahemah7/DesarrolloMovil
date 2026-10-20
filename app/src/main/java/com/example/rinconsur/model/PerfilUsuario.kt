package com.example.rinconsur.model

/**
 * Perfiles del caso (catalogo `rol`), con su codigo de la base.
 *
 * La app **solo permite entrar** como supervisor (P-03) u observador (P-04): son los dos perfiles
 * que operan el conteo, que es lo que esta entrega cubre. La jefa de tienda y el subgerente existen
 * en los datos de la empresa y se usan mas adelante; ellos miran ingresos, boletas y meta, que
 * llegan del sistema central como dato de referencia, no se registran desde la app.
 */
enum class PerfilUsuario(val codigo: String, val etiqueta: String, val puedeEntrar: Boolean) {
    JEFE_TIENDA("P-01", "Jefa de tienda", false),
    SUBGERENTE("P-02", "Subgerente comercial", false),
    SUPERVISOR("P-03", "Supervisor de piso", true),
    OBSERVADOR("P-04", "Colaborador observador", true);

    companion object {
        fun porCodigo(codigo: String): PerfilUsuario? = entries.firstOrNull { it.codigo == codigo }
    }
}
