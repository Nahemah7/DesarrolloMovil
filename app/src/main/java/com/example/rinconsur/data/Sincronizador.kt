package com.example.rinconsur.data

import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionRegistro
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.model.RegistroPendiente

class Sincronizador(
    private val repositorio: FlujoRepository,
    private val tokens: TokenPreferencias,
    private val direccion: DireccionServidor
) {

    data class Resultado(val enviados: Int, val quedan: Int, val motivo: String? = null)

    suspend fun enviarPendientes(): Resultado {
        val token = tokens.leer() ?: return Resultado(0, repositorio.contarRegistrosPendientes(),
            "Sin sesión en el servidor: los registros quedan en el teléfono.")
        val pendientes = repositorio.registrosPendientes()
        if (pendientes.isEmpty()) return Resultado(0, 0)

        val remoto = ClienteRemoto(direccion.leer())
        var enviados = 0
        var problema: String? = null

        for (registro in pendientes) {
            val idSesionServidor = registro.idSesionRemota
            if (idSesionServidor == null) {

                problema = "La sesión de esta medición todavía no está en el servidor."
                continue
            }
            val resultado = enviar(remoto, token, registro, idSesionServidor)
            when {
                resultado.id != null -> {
                    repositorio.marcarRegistroEnviado(registro.idLocal, resultado.id)
                    enviados++
                }
                resultado.definitivo -> {

                    problema = resultado.motivo
                    continue
                }
                else -> {

                    problema = resultado.motivo
                    break
                }
            }
        }
        return Resultado(enviados, repositorio.contarRegistrosPendientes(), problema)
    }

    private data class Envio(val id: Long?, val definitivo: Boolean, val motivo: String?)

    private suspend fun enviar(
        cliente: ClienteRemoto,
        token: String,
        registro: RegistroPendiente,
        idSesionServidor: Long
    ): Envio {
        return try {
            val respuesta = cliente.medicion.registrar(
                "Bearer " + token,
                PeticionRegistro(
                    idSesion = idSesionServidor.toInt(),
                    idClasificacion = registro.idClasificacion.toInt(),
                    idRango = registro.idRango.toInt(),
                    idTipoCompania = registro.idTipoCompania.toInt(),
                    comprasOtraTienda = registro.comprasOtraTienda,
                    idAccesoDestino = registro.idAccesoDestino?.toInt(),
                    fechaHora = registro.fechaHora
                )
            )
            val cuerpo = respuesta.body()
            when {
                respuesta.isSuccessful && cuerpo?.id != null ->
                    Envio(cuerpo.id.toLong(), definitivo = true, motivo = null)

                respuesta.code() in 400..499 ->
                    Envio(null, definitivo = true, motivo = "El servidor rechazó un registro (" + respuesta.code() + ").")
                else ->
                    Envio(null, definitivo = false, motivo = "El servidor respondió " + respuesta.code() + ".")
            }
        } catch (e: Exception) {
            Envio(null, definitivo = false, motivo = "Sin conexión: los registros esperan en el teléfono.")
        }
    }
}
