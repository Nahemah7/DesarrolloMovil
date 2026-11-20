package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen

// Traduce lo que devuelve el servicio de medición al modelo que ya usan las pantallas.
// El estado llega como texto ("Validada") y la app trabaja con su enum, así que la conversión
// vive acá y no se repite en cada pantalla.
fun SesionRemota.aResumen(): SesionResumen = SesionResumen(
    id = id?.toLong() ?: 0L,
    codigo = codigo.orEmpty(),
    idAcceso = idAcceso?.toLong() ?: 0L,
    fecha = fecha.orEmpty(),
    horaInicioProg = horaInicio.orEmpty(),
    horaFinProg = horaFin.orEmpty(),
    estado = EstadoSesion.porNombre(estado.orEmpty()),
    idObservador = 0L,
    nombreObservador = observador.orEmpty(),
    registrosValidos = (registros ?: 0L).toInt()
)
