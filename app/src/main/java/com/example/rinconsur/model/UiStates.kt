package com.example.rinconsur.model

import com.example.rinconsur.model.PreguntaSeguridad

/** Estado de la pantalla de inicio de sesion. */
data class LoginUiState(
    val rut: String = "",
    val clave: String = "",
    val claveVisible: Boolean = false,
    /** Error del campo RUT: solo para el formato (el numero que escribio la persona). */
    val errorRut: String? = null,
    val errorClave: String? = null,
    /**
     * Error de autenticacion, a proposito generico: no dice si el RUT existe, si la cuenta esta
     * activa ni cual de los dos datos fallo. Decirlo seria regalarle a cualquiera la lista de
     * RUT validos del personal (enumeracion de usuarios).
     */
    val errorGeneral: String? = null
)

/** Estado del registro de personas (pantalla 03). */
data class RegistroUiState(
    val codigoSesion: String = "",
    val clasificacion: Clasificacion = Clasificacion.TRANSITO,
    val rangoEtario: RangoEtario? = null,
    val compania: Compania = Compania.SOLO,
    val comprasOtraTienda: Boolean = false,
    val destino: Long? = null,
    val resumen: ResumenSesion = ResumenSesion(codigo = ""),
    val ultimosRegistros: List<Registro> = emptyList(),
    /** Accesos de la tienda, para decir hacia donde se dirige un transito. */
    val accesos: List<Acceso> = emptyList(),
    /** Solo se puede anular mientras la sesion esta en curso (RN-11). */
    val sesionEnCurso: Boolean = false,
    /** Sin sesion en curso no se registra nada: hay que iniciar una desde Mi sesion. */
    val sinSesionEnCurso: Boolean = false,
    val mensaje: String? = null
) {
    /** En hora punta se registra con la plantilla y un solo toque: la rangoEtario puede venir puesta. */
    val puedeGuardar: Boolean get() = rangoEtario != null &&
        (clasificacion != Clasificacion.TRANSITO || destino != null)
}

/** Estado de la pantalla de sesiones del supervisor (pantalla 06). */
data class SesionesUiState(
    /** Las del dia de trabajo. */
    val sesiones: List<SesionResumen> = emptyList(),
    /** Las cerradas que esperan revision, de cualquier fecha: son las que hay que resolver. */
    val porValidar: List<SesionResumen> = emptyList(),
    /** Las de los ultimos siete dias, para mirar la semana. */
    val deLaSemana: List<SesionResumen> = emptyList(),
    val bloquesPendientes: Int = 0,
    val pendienteMasAtrasado: String = "",
    val filtro: String = "Día",
    /** Dia que se esta mirando, en yyyy-MM-dd. */
    val fecha: String = "",
    /** El mismo dia escrito para leerlo: "martes 15 de septiembre". */
    val fechaLarga: String = ""
) {
    /** Lo que se muestra segun el filtro elegido. */
    val visibles: List<SesionResumen>
        get() = when (filtro) {
            "Por validar" -> porValidar
            else -> sesiones
        }
}

/** Estado de los indicadores del dia (pantalla 07). */
data class IndicadoresUiState(
    val indicadores: IndicadoresDia? = null,
    val porAcceso: List<TransitoPorAcceso> = emptyList(),
    val periodo: TipoPeriodo = TipoPeriodo.HOY,
    val tipoDia: String = "Habil",
    /** La fecha que se esta mirando: hoy, no el ultimo dia con ingresos cargados. */
    val fecha: String = "",
    /** Las sesiones de hoy: es lo unico que se puede mostrar cuando la tienda aun no cargo el dia. */
    val sesionesHoy: List<SesionResumen> = emptyList(),
    /** Si los dias habiles ya cargados tienen cifras: sirve para avisar cuando hoy no hay nada. */
    val hayDatosEnHabiles: Boolean = false,
    /** Las semanas con dias cargados, de la mas nueva a la mas vieja. */
    val semanas: List<SemanaIndicadores> = emptyList(),
    /** Donde esta parado el filtro: 0 = todas las semanas, 1 = la mas nueva, y sigue hacia atras. */
    val posicionSemana: Int = 0
) {
    /** La semana que se esta mirando; null cuando el filtro esta en "todas". */
    val semanaElegida: SemanaIndicadores?
        get() = semanas.getOrNull(posicionSemana - 1)

    val haySemanaAnterior: Boolean get() = posicionSemana < semanas.size
    val haySemanaSiguiente: Boolean get() = posicionSemana > 0
}

/** Estado del registro de un evento del dia (formulario con validacion por campo). */
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
    /** Los eventos del dia, con su foto y si estan anulados. */
    val eventos: List<EventoDia> = emptyList(),
    val guardado: Boolean = false,
    val mensaje: String? = null
) {
    val tieneFoto: Boolean get() = fotoBytes != null && fotoBytes.isNotEmpty()
}

/** Estado del formulario para rechazar una sesion (casillas de motivo + validacion por campo). */
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

/**
 * Programar una sesion de conteo (supervisor de piso). El acceso y el observador se eligen de las
 * listas de la base; la duracion se calcula sola y el choque con otra sesion se avisa antes de
 * programar.
 */
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
    /** Minutos entre las dos horas elegidas. */
    val duracionMinutos: Int get() = minutos(horaFin) - minutos(horaInicio)

    val duracionValida: Boolean get() = duracionMinutos in 30..60

    /** La sesion que se cruza, si hay: mismo acceso o misma persona en horas que se pisan. */
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

/** Paso del formulario para recuperar la clave sin correo. */
enum class PasoRecuperar { RUT, PREGUNTAS, CLAVE, LISTO }

/**
 * Estado de la recuperacion de clave. El error general es a proposito generico, por la misma razon
 * que en el login: no se dice si el RUT existe ni si esa persona tiene preguntas guardadas.
 */
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

/** Estado del perfil: los datos de quien entro y la seguridad de su cuenta. */
data class PerfilUiState(
    val nombre: String = "",
    val apellido: String = "",
    val errorNombre: String? = null,
    val avisoNombre: String? = null,
    val rut: String = "",
    val cargo: String = "",
    val perfil: String = "",
    val foto: ByteArray? = null,
    // Cambio de clave
    val claveActual: String = "",
    val claveNueva: String = "",
    val repetirNueva: String = "",
    val claveVisible: Boolean = false,
    val errorActual: String? = null,
    val errorNueva: String? = null,
    val errorRepetir: String? = null,
    val avisoClave: String? = null,
    // Preguntas de seguridad
    val catalogo: List<PreguntaSeguridad> = emptyList(),
    val elegidas: List<Long> = emptyList(),
    val respuestas: Map<Long, String> = emptyMap(),
    val tienePreguntas: Boolean = false,
    val avisoFoto: String? = null,
    val errorPreguntas: String? = null,
    val avisoPreguntas: String? = null
)
