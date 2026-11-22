package com.example.rinconsur.model

data class RegistroPendiente(
    val idLocal: Long,
    val idSesion: Long,

    val idSesionRemota: Long?,
    val fechaHora: String,
    val idClasificacion: Long,
    val idRango: Long,
    val idTipoCompania: Long,
    val comprasOtraTienda: Boolean,
    val idAccesoDestino: Long?
)
