package com.example.rinconsur.model

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
    val filtro: String = "Hoy"
) {
    /** Lo que se muestra segun el filtro elegido. */
    val visibles: List<SesionResumen>
        get() = when (filtro) {
            "Por validar" -> porValidar
            "Semana" -> deLaSemana
            else -> sesiones
        }
}

/** Estado de los indicadores del dia (pantalla 07). */
data class IndicadoresUiState(
    val indicadores: IndicadoresDia? = null,
    val porAcceso: List<TransitoPorAcceso> = emptyList(),
    val periodo: String = "Hoy",
    val tipoDia: String = "Habil"
)

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

/** Estado de la pantalla de sesion iniciada. */
data class EntradaUiState(
    val nombre: String = "",
    val rut: String = "",
    val perfil: PerfilUsuario = PerfilUsuario.OBSERVADOR,
    val accesos: List<Acceso> = emptyList()
)

