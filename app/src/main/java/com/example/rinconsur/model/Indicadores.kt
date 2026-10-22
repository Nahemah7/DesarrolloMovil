package com.example.rinconsur.model

/** Indicadores del dia calculados por las vistas SQL (RN-12 a RN-15). */
data class IndicadoresDia(
    val fecha: String,
    val tipoDia: String,
    val ingresos: Int,
    val transacciones: Int,
    val conversionBruta: Double?,
    val estadoDia: String,
    val transito: Double?,
    val flujoSinTransito: Double?,
    val conversionSinTransito: Double?,
    val bloquesMedidos: Int,
    val bloquesEstimados: Int,
    val bloquesSinInformacion: Int
) {
    val coberturaTexto: String
        get() = "$bloquesMedidos medidos · $bloquesEstimados estimados · $bloquesSinInformacion sin información"

    val conversionSinTransitoTexto: String
        get() = conversionSinTransito?.let { "${formato(it)} %" } ?: "no disponible"

    companion object {
        /** 8.7 -> "8,7" (coma decimal, como se escribe en Chile). */
        fun formato(v: Double): String = v.toString().replace('.', ',')

        /** 5735 -> "5.735" (miles con punto). */
        fun formatoMiles(v: Int): String = String.format("%,d", v).replace(',', '.')
    }
}

/** Una fila del transito por acceso (pantalla de indicadores y horas punta). */
data class TransitoPorAcceso(
    val acceso: String,
    val ingresos: Int,
    val transitoEstimado: Double?,
    val origen: OrigenTransito,
    val idBloque: Long = 0,
    val hora: String = ""
)
