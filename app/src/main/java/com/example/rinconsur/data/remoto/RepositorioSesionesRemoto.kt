package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.SesionResumen

class RepositorioSesionesRemoto(
    direccion: String,
    private val token: String
) {
    private val cliente = ClienteRemoto(direccion)
    private val autorizacion = "Bearer " + token

    suspend fun sesionesDeFecha(fecha: String): List<SesionResumen>? = try {
        val respuesta = cliente.medicion.sesionesDelDia(autorizacion, fecha)
        if (respuesta.isSuccessful) respuesta.body()?.map { it.aResumen() } else null
    } catch (e: Exception) {
        null
    }

    suspend fun porValidar(): List<SesionResumen>? = try {
        val respuesta = cliente.medicion.porValidar(autorizacion)
        if (respuesta.isSuccessful) respuesta.body()?.map { it.aResumen() } else null
    } catch (e: Exception) {
        null
    }

    suspend fun editar(
        idSesionRemota: Long,
        idAcceso: Long,
        horaInicio: String,
        horaFin: String,
        idObservador: Long,
        nombreObservador: String?
    ): Boolean = try {
        val respuesta = cliente.medicion.editar(
            autorizacion,
            idSesionRemota.toInt(),
            PeticionEditarSesion(
                idAcceso = idAcceso.toInt(),
                horaInicio = horaInicio,
                horaFin = horaFin,
                idObservador = idObservador.toInt(),
                observadorNombre = nombreObservador
            )
        )
        respuesta.isSuccessful
    } catch (e: Exception) {
        false
    }

    suspend fun iniciar(idSesionRemota: Long): Boolean = try {
        val respuesta = cliente.medicion.iniciar(autorizacion, idSesionRemota.toInt())
        respuesta.isSuccessful || respuesta.code() == 409
    } catch (e: Exception) {
        false
    }

    suspend fun validar(idSesion: Long): Boolean = try {
        val respuesta = cliente.medicion.validar(autorizacion, idSesion.toInt())
        respuesta.isSuccessful
    } catch (e: Exception) {
        false
    }
}
