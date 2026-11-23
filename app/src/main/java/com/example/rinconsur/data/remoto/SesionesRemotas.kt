package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen

fun SesionRemota.aResumen(): SesionResumen = SesionResumen(
    id = id?.toLong() ?: 0L,
    codigo = codigo.orEmpty(),
    idAcceso = idAcceso?.toLong() ?: 0L,
    fecha = fecha.orEmpty(),
    horaInicioProg = horaInicio.orEmpty(),
    horaFinProg = horaFin.orEmpty(),
    estado = EstadoSesion.porNombre(estado.orEmpty()),
    idObservador = idObservador?.toLong() ?: 0L,
    nombreObservador = observador.orEmpty(),
    registrosValidos = (registros ?: 0L).toInt(),
    inicioReal = inicioReal?.replace("T", " ")?.take(19),
    finReal = finReal?.replace("T", " ")?.take(19),
    idRemoto = id?.toLong()
)
