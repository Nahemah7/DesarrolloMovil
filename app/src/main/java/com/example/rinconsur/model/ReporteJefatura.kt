package com.example.rinconsur.model

data class ConversionMes(
    val mes: String,
    val ingresos: Int,
    val boletas: Int,
    val conversion: Double?,
    val dias: Int
) {
    val periodo: String
        get() {
            val nombres = listOf(
                "enero", "febrero", "marzo", "abril", "mayo", "junio",
                "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
            )
            val partes = mes.split("-")
            val indice = (partes.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            return (nombres.getOrNull(indice) ?: mes) + " " + (partes.getOrNull(0) ?: "")
        }

    val conversionTexto: String get() = conversion?.let { formatoPorcentaje(it) } ?: "sin dato"
}

data class MetaDelMes(
    val anio: Int,
    val mes: Int,
    val objetivo: Double?,
    val actual: Double?,
    val dias: Int = 0
) {
    val registrada: Boolean get() = objetivo != null

    val cumplimiento: Double?
        get() {
            val o = objetivo ?: return null
            val a = actual ?: return null
            if (o <= 0) return null
            return Math.round(a / o * 1000.0) / 10.0
        }

    val avance: Float
        get() {
            val c = cumplimiento ?: return 0f
            return (c / 100.0).toFloat().coerceIn(0f, 1f)
        }

    val cumplimientoTexto: String get() = cumplimiento?.let { formatoPorcentaje(it) } ?: "sin dato"
}

data class HoraPunta(
    val hora: String,
    val ingresos: Int,
    val transito: Int,
    val medidos: Int,
    val bloques: Int
) {
    val medido: Boolean get() = medidos > 0
}

data class ReporteJefatura(
    val meta: MetaDelMes,
    val meses: List<ConversionMes> = emptyList(),
    val horas: List<HoraPunta> = emptyList(),
    val porRango: List<GrupoConteo> = emptyList(),
    val porCompania: List<GrupoConteo> = emptyList(),
    val compras: List<GrupoConteo> = emptyList()
) {
    val hayDatos: Boolean get() = meses.isNotEmpty()

    val horaPunta: HoraPunta?
        get() = horas.filter { it.medido }.maxByOrNull { it.transito }
}

fun formatoPorcentaje(valor: Double): String =
    String.format(java.util.Locale("es", "CL"), "%.1f %%", valor)
