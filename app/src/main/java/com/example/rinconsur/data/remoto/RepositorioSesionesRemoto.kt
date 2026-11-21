package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.SesionResumen

// Envuelve las llamadas de sesiones contra el gateway. Devuelve null cuando no se pudo hablar
// con el servidor, y quien lo usa decide qué hacer: en esta app, caer al SQLite local.
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

    // Marca el inicio en el servidor. Devuelve true si quedó en curso o si ya lo estaba: para
    // la app las dos cosas significan lo mismo y no hay que distinguirlas.
    suspend fun iniciar(idSesionRemota: Long): Boolean = try {
        val respuesta = cliente.medicion.iniciar(autorizacion, idSesionRemota.toInt())
        respuesta.isSuccessful || respuesta.code() == 409
    } catch (e: Exception) {
        false
    }

    // Valida una sesión cerrada. El revisor sale del token, así que la app no lo manda.
    suspend fun validar(idSesion: Long): Boolean = try {
        val respuesta = cliente.medicion.validar(autorizacion, idSesion.toInt())
        respuesta.isSuccessful
    } catch (e: Exception) {
        false
    }
}
