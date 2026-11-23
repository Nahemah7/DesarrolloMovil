package com.example.rinconsur.model

data class PersonalRendimiento(
    val idObservador: Long = 0,
    val observador: String = "",
    val sesiones: Int = 0,
    val cerradas: Int = 0,
    val validadas: Int = 0,
    val rechazadas: Int = 0,
    val registros: Int = 0,
    val transito: Int = 0,
    val ultimaFecha: String? = null
) {
    val pctTransito: Double?
        get() = if (registros > 0) transito * 100.0 / registros else null

    val pctTransitoTexto: String
        get() = pctTransito?.let { String.format(java.util.Locale("es", "CL"), "%.1f %%", it) } ?: "sin dato"
}
