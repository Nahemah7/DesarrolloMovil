package com.example.rinconsur.model

// Un registro que ya está guardado en el teléfono pero todavía no llegó al servidor.
data class RegistroPendiente(
    val idLocal: Long,
    val idSesion: Long,
    // Id de la misma sesión en el servidor; nulo si todavía no está allá.
    val idSesionRemota: Long?,
    val fechaHora: String,
    val idClasificacion: Long,
    val idRango: Long,
    val idTipoCompania: Long,
    val comprasOtraTienda: Boolean,
    val idAccesoDestino: Long?
)
