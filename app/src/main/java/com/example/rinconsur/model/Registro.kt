package com.example.rinconsur.model

/**
 * Una persona contada. Se registra en menos de 3 segundos y sin escribir (RNF-01):
 * clasificacion, tramo, compania y, si es transito, a que acceso se dirige.
 * Nada identifica a la persona (RN-17).
 */
data class Registro(
    val id: Long,
    val idSesion: Long,
    val codigoSesion: String,
    val fechaHora: String,
    val hora: String,
    val clasificacion: Clasificacion,
    val rangoEtario: RangoEtario,
    val compania: Compania,
    val comprasOtraTienda: Boolean,
    /** Acceso destino: solo si es transito y distinto del de entrada. */
    val seDirige: Long? = null,
    // Anulacion con autorizacion: no se borra, se anula y queda quien y por que
    val anulado: Boolean = false,
    val idUsuarioAnula: Long? = null,
    val fechaAnulacion: String? = null,
    val motivoAnulacion: String? = null
)
