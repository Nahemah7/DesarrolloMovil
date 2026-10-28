package com.example.rinconsur.model

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
    val bloquesSinInformacion: Int,
    val dias: Int = 1,
    val desde: String = fecha,
    val hasta: String = fecha
) {
    val coberturaTexto: String
        get() = "$bloquesMedidos medidos · $bloquesEstimados estimados · $bloquesSinInformacion sin información"

    val conversionSinTransitoTexto: String
        get() = conversionSinTransito?.let { "${formato(it)} %" } ?: "no disponible"

    companion object {
        fun formato(v: Double): String = v.toString().replace('.', ',')

        fun formatoMiles(v: Int): String = String.format("%,d", v).replace(',', '.')
    }
}

data class TransitoPorAcceso(
    val acceso: String,
    val ingresos: Int,
    val transitoEstimado: Double?,
    val origen: OrigenTransito,
    val idBloque: Long = 0,
    val hora: String = ""
)
