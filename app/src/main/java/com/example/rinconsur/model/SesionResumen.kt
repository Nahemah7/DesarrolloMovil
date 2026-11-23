package com.example.rinconsur.model

data class SesionResumen(
    val id: Long,
    val codigo: String,
    val idAcceso: Long,
    val fecha: String,
    val horaInicioProg: String,
    val horaFinProg: String,
    val inicioReal: String? = null,
    val finReal: String? = null,
    val estado: EstadoSesion = EstadoSesion.PROGRAMADA,
    val idObservador: Long = 0,
    val idProgramador: Long = 0,
    val idRevisor: Long? = null,
    val fechaRevision: String? = null,
    val motivoRechazo: String? = null,
    val nombreObservador: String = "",
    val registrosValidos: Int = 0,
    val idRemoto: Long? = null,
    val transito: Int = 0,
    val visita: Int = 0,
    val noDeterminado: Int = 0,
    val pctTransito: Double? = null
) {
    val horaInicioCorta: String get() = horaInicioProg.take(5)
    val horaFinCorta: String get() = horaFinProg.take(5)
    val duracionMinutos: Int get() = minutos(horaInicioProg) .let { inicio -> minutos(horaFinProg) - inicio }

    private fun minutos(hora: String): Int {
        val partes = hora.split(":")
        return partes[0].toInt() * 60 + partes.getOrElse(1) { "0" }.toInt()
    }

    val sePuedeValidar: Boolean get() = registrosValidos >= 30

    fun horasEsperando(ahora: Long = System.currentTimeMillis()): Long? {
        val marca = finReal ?: return null
        val formato = com.example.rinconsur.data.FechaTienda.formato("yyyy-MM-dd HH:mm:ss")
        val cuando = runCatching { formato.parse(marca) }.getOrNull() ?: return null
        val horas = (ahora - cuando.time) / 3_600_000L
        return if (horas < 0) 0 else horas
    }

    fun esperaTexto(ahora: Long = System.currentTimeMillis()): String? {
        val horas = horasEsperando(ahora) ?: return null
        return when {
            horas < 1 -> "hace menos de una hora"
            horas < 24 -> "hace " + horas + if (horas == 1L) " hora" else " horas"
            else -> {
                val dias = horas / 24
                "hace " + dias + if (dias == 1L) " día" else " días"
            }
        }
    }

    val sinPublicar: Boolean get() = idRemoto == null && estado == EstadoSesion.PROGRAMADA
}
