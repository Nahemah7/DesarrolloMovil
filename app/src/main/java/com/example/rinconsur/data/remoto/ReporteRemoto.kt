package com.example.rinconsur.data.remoto

data class ReporteRemoto(
    val meta: MetaRemota? = null,
    val meses: List<MesRemoto> = emptyList(),
    val horas: List<HoraRemota> = emptyList(),
    val porRango: List<GrupoRemoto> = emptyList(),
    val porCompania: List<GrupoRemoto> = emptyList(),
    val compras: List<GrupoRemoto> = emptyList()
)

data class MetaRemota(
    val anio: Int = 0,
    val mes: Int = 0,
    val objetivo: Double? = null,
    val actual: Double? = null,
    val dias: Int = 0
)

data class MesRemoto(
    val mes: String = "",
    val ingresos: Int = 0,
    val boletas: Int = 0,
    val conversion: Double? = null,
    val dias: Int = 0
)

data class HoraRemota(
    val idBloque: Int = 0,
    val ingresos: Int = 0,
    val transito: Int = 0,
    val medidos: Int = 0,
    val bloques: Int = 0
)

data class GrupoRemoto(
    val etiqueta: String = "",
    val cantidad: Int = 0,
    val porcentaje: Double? = null
)
