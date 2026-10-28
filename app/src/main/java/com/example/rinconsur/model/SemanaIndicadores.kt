package com.example.rinconsur.model

data class SemanaIndicadores(val desde: String, val hasta: String) {

    val etiqueta: String
        get() {
            val d1 = diaDe(desde)
            val d2 = diaDe(hasta)
            val mes = mesCorto(hasta)
            return if (mesCorto(desde) == mes) "$d1-$d2 $mes" else "$d1 ${mesCorto(desde)} - $d2 $mes"
        }

    val titulo: String
        get() = "Semana del " + diaDe(desde) + " de " + mesLargo(desde) +
            " hasta el " + diaDe(hasta) + " de " + mesLargo(hasta)

    private fun diaDe(iso: String): String = iso.takeLast(2).trimStart('0').ifEmpty { "0" }

    private fun mesLargo(iso: String): String {
        val m = iso.substring(5, 7).toIntOrNull() ?: return ""
        return listOf(
            "enero", "febrero", "marzo", "abril", "mayo", "junio",
            "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
        ).getOrElse(m - 1) { "" }
    }

    private fun mesCorto(iso: String): String {
        val m = iso.substring(5, 7).toIntOrNull() ?: return ""
        return listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
            .getOrElse(m - 1) { "" }
    }
}
