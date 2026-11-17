package com.example.rinconsur.model

data class GrupoConteo(
    val etiqueta: String,
    val cantidad: Int,
    val porcentaje: Double? = null
) {
    val porcentajeTexto: String
        get() = porcentaje?.let { String.format(java.util.Locale("es", "CL"), "%.1f %%", it) } ?: "sin dato"

    val fraccion: Float
        get() = ((porcentaje ?: 0.0) / 100.0).toFloat().coerceIn(0f, 1f)
}

data class DetalleSesion(
    val sesion: SesionResumen,
    val registrosValidos: Int = 0,
    val anulados: Int = 0,
    val primeraMarca: String? = null,
    val ultimaMarca: String? = null,
    val porClasificacion: List<GrupoConteo> = emptyList(),
    val porRango: List<GrupoConteo> = emptyList(),
    val porCompania: List<GrupoConteo> = emptyList(),
    val comprasOtraTienda: List<GrupoConteo> = emptyList(),
    val destinosTransito: List<GrupoConteo> = emptyList()
) {
    val sinRegistros: Boolean get() = registrosValidos == 0

    val horaPrimera: String? get() = primeraMarca?.takeLast(8)?.take(5)
    val horaUltima: String? get() = ultimaMarca?.takeLast(8)?.take(5)

    val duracionRealMinutos: Int?
        get() {
            val desde = primeraMarca ?: return null
            val hasta = ultimaMarca ?: return null
            val formato = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US)
            val inicio = formato.parse(desde) ?: return null
            val fin = formato.parse(hasta) ?: return null
            return ((fin.time - inicio.time) / 60000L).toInt()
        }
}
