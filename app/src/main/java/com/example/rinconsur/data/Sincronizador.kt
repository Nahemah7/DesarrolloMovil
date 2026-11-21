package com.example.rinconsur.data

import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionRegistro
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.model.RegistroPendiente

// Sube los registros anotados sin red. La regla es simple y en este orden:
// 1. un registro que ya subió se marca y no se vuelve a mandar (la marca es el estado, no la memoria);
// 2. si una llamada falla, se corta el envío: no tiene sentido seguir empujando contra una red caída;
// 3. un rechazo del servidor (400/404/409) no se reintenta para siempre: se deja en la cola y se informa.
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
                // La sesión todavía existe solo en el teléfono: sin su id en el servidor no hay
                // dónde colgar el registro, así que espera sin gastar intentos.
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
                    // El servicio lo rechazó por una razón de negocio: reintentarlo no lo arregla.
                    problema = resultado.motivo
                    continue
                }
                else -> {
                    // Sin red o servicio caído: se corta acá y se reintenta en la próxima pasada.
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
                // Un 4xx es una respuesta del negocio: reintentar no lo va a cambiar.
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
