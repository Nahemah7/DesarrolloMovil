package com.example.rinconsur.data.remoto

import com.example.rinconsur.data.FlujoRepository

class SincronizadorSesiones(
    private val repositorio: FlujoRepository,
    private val tokens: TokenPreferencias,
    private val direccion: DireccionServidor
) {

    suspend fun publicarPendientes(): Int {
        val token = tokens.leer() ?: return 0
        val pendientes = repositorio.sesionesSinPublicar(repositorio.fechaHoy())
        if (pendientes.isEmpty()) return 0
        val cliente = ClienteRemoto(direccion.leer())
        var publicadas = 0
        for ((idLocal, sesion) in pendientes) {
            val respuesta = runCatching {
                cliente.medicion.programar(
                    "Bearer " + token,
                    PeticionSesionNueva(
                        idAcceso = sesion.idAcceso.toInt(),
                        fecha = sesion.fecha,
                        horaInicio = sesion.horaInicioProg,
                        horaFin = sesion.horaFinProg,
                        idObservador = sesion.idObservador.toInt(),
                        observadorNombre = sesion.nombreObservador
                    )
                )
            }.getOrNull() ?: break
            val cuerpo = respuesta.body()
            if (!respuesta.isSuccessful || cuerpo?.id == null) break
            repositorio.adoptarSesionDelServicio(
                idLocal,
                cuerpo.id.toLong(),
                cuerpo.codigo ?: sesion.codigo
            )
            publicadas++
        }
        return publicadas
    }
}
