package com.example.rinconsur.model

import com.example.rinconsur.model.PreguntaSeguridad

data class LoginUiState(
    val rut: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    val errorRut: String? = null,
    val errorClave: String? = null,
    val errorGeneral: String? = null
)

data class RegistroUiState(
    val codigoSesion: String = "",
    val clasificacion: Clasificacion = Clasificacion.TRANSITO,
    val rangoEtario: RangoEtario? = null,
    val compania: Compania = Compania.SOLO,
    val comprasOtraTienda: Boolean = false,
    val destino: Long? = null,
    val resumen: ResumenSesion = ResumenSesion(codigo = ""),
    val ultimosRegistros: List<Registro> = emptyList(),
    val accesos: List<Acceso> = emptyList(),
    val sesionEnCurso: Boolean = false,
    val sinSesionEnCurso: Boolean = false,
    val mensaje: String? = null
) {
    val puedeGuardar: Boolean get() = rangoEtario != null &&
        (clasificacion != Clasificacion.TRANSITO || destino != null)
}

data class SesionesUiState(
    val sesiones: List<SesionResumen> = emptyList(),
    val porValidar: List<SesionResumen> = emptyList(),
    val deLaSemana: List<SesionResumen> = emptyList(),
    val bloquesPendientes: Int = 0,
    val pendienteMasAtrasado: String = "",
    val filtro: String = "Día",
    val fecha: String = "",
    val fechaLarga: String = ""
) {
    val visibles: List<SesionResumen>
        get() = when (filtro) {
            "Por validar" -> porValidar
            else -> sesiones
        }
}

data class IndicadoresUiState(
    val indicadores: IndicadoresDia? = null,
    val porAcceso: List<TransitoPorAcceso> = emptyList(),
    val periodo: TipoPeriodo = TipoPeriodo.HOY,
    val tipoDia: String = "Habil",
    val fecha: String = "",
    val sesionesHoy: List<SesionResumen> = emptyList(),
    val hayDatosEnHabiles: Boolean = false,
    val semanas: List<SemanaIndicadores> = emptyList(),
    val posicionSemana: Int = 0
) {
    val semanaElegida: SemanaIndicadores?
        get() = semanas.getOrNull(posicionSemana - 1)

    val haySemanaAnterior: Boolean get() = posicionSemana < semanas.size
    val haySemanaSiguiente: Boolean get() = posicionSemana > 0
}

data class EventoUiState(
    val fecha: String = "",
    val tipo: TipoEvento = TipoEvento.CIERRE_ACCESO,
    val idAcceso: Long? = null,
    val descripcion: String = "",
    val fotoBytes: ByteArray? = null,
    val errorFecha: String? = null,
    val errorDescripcion: String? = null,
    val errorAcceso: String? = null,
    val eventosDelDia: Int = 0,
    val eventos: List<EventoDia> = emptyList(),
    val guardado: Boolean = false,
    val mensaje: String? = null
) {
    val tieneFoto: Boolean get() = fotoBytes != null && fotoBytes.isNotEmpty()
}

data class RechazarUiState(
    val codigo: String = "",
    val acceso: String = "",
    val observador: String = "",
    val hora: String = "",
    val registros: Int = 0,
    val pctTransito: Double? = null,
    val motivos: Set<MotivoRechazo> = emptySet(),
    val otroMotivo: String = "",
    val errorMotivos: String? = null,
    val errorOtro: String? = null,
    val rechazada: Boolean = false
) {
    val exigeOtro: Boolean get() = motivos.contains(MotivoRechazo.OTRO)
}

data class ProgramarUiState(
    val fecha: String = "",
    val accesos: List<Acceso> = emptyList(),
    val observadores: List<Colaborador> = emptyList(),
    val sesionesDeLaFecha: List<SesionResumen> = emptyList(),
    val idAcceso: Long? = null,
    val horaInicio: String = "13:00",
    val horaFin: String = "13:40",
    val idObservador: Long? = null,
    val error: String? = null,
    val programada: String = "",
    val codigo: String = ""
) {
    val duracionMinutos: Int get() = minutos(horaFin) - minutos(horaInicio)

    val duracionValida: Boolean get() = duracionMinutos in 30..60

    val choque: SesionResumen? get() {
        if (idAcceso == null) return null
        return sesionesDeLaFecha.firstOrNull { s ->
            s.idAcceso == idAcceso && seCruzan(s.horaInicioProg, s.horaFinProg, horaInicio, horaFin)
        } ?: idObservador?.let { id ->
            sesionesDeLaFecha.firstOrNull { s ->
                s.idObservador == id && seCruzan(s.horaInicioProg, s.horaFinProg, horaInicio, horaFin)
            }
        }
    }

    val sePuedeProgramar: Boolean
        get() = idAcceso != null && idObservador != null && duracionValida && choque == null

    private companion object {
        fun minutos(hora: String): Int {
            val p = hora.split(":")
            return (p.getOrNull(0)?.toIntOrNull() ?: 0) * 60 + (p.getOrNull(1)?.toIntOrNull() ?: 0)
        }

        fun seCruzan(inicioA: String, finA: String, inicioB: String, finB: String): Boolean =
            minutos(inicioA) < minutos(finB) && minutos(inicioB) < minutos(finA)
    }
}

enum class PasoRecuperar { RUT, PREGUNTAS, CLAVE, LISTO }

data class RecuperarUiState(
    val paso: PasoRecuperar = PasoRecuperar.RUT,
    val rut: String = "",
    val preguntas: List<PreguntaSeguridad> = emptyList(),
    val respuestas: List<String> = listOf("", ""),
    val clave: String = "",
    val repetir: String = "",
    val claveVisible: Boolean = false,
    val errorRut: String? = null,
    val errorRespuestas: String? = null,
    val errorClave: String? = null,
    val errorRepetir: String? = null,
    val errorGeneral: String? = null
)

data class PerfilUiState(
    val nombre: String = "",
    val apellido: String = "",
    val errorNombre: String? = null,
    val avisoNombre: String? = null,
    val rut: String = "",
    val cargo: String = "",
    val perfil: String = "",
    val foto: ByteArray? = null,
    val claveActual: String = "",
    val claveNueva: String = "",
    val repetirNueva: String = "",
    val claveVisible: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorRepetir: String? = null,
    val avisoClave: String? = null,
    val catalogo: List<PreguntaSeguridad> = emptyList(),
    val elegidas: List<Long> = emptyList(),
    val respuestas: Map<Long, String> = emptyMap(),
    val tienePreguntas: Boolean = false,
    val avisoFoto: String? = null,
    val errorPreguntas: String? = null,
    val avisoPreguntas: String? = null
)
