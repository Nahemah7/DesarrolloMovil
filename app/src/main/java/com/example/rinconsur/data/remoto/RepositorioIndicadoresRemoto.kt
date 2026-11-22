package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.OrigenTransito
import com.example.rinconsur.model.TransitoPorAcceso

class RepositorioIndicadoresRemoto(
    direccion: String,
    private val token: String
) {
    private val cliente = ClienteRemoto(direccion)
    private val autorizacion = "Bearer " + token

    suspend fun delDia(fecha: String): IndicadoresDia? = try {
        val respuesta = cliente.medicion.indicadorDelDia(autorizacion, fecha)
        if (respuesta.isSuccessful) respuesta.body()?.let { it.aIndicadores(fecha, fecha) } else null
    } catch (e: Exception) {
        null
    }

    suspend fun delPeriodo(tipoDia: String, desde: String? = null, hasta: String? = null): IndicadoresDia? = try {
        val respuesta = cliente.medicion.indicadoresDelTipo(autorizacion, tipoDia, desde, hasta)
        if (respuesta.isSuccessful) {
            respuesta.body()?.let { it.aIndicadores(desde ?: it.desde ?: "", hasta ?: it.hasta ?: "") }
        } else null
    } catch (e: Exception) {
        null
    }

    suspend fun accesosDelDia(fecha: String): List<TransitoPorAcceso>? = try {
        val respuesta = cliente.medicion.accesosDelDia(autorizacion, fecha)
        if (respuesta.isSuccessful) respuesta.body()?.map { it.aTransito() } else null
    } catch (e: Exception) {
        null
    }

    suspend fun accesosDelPeriodo(tipoDia: String, desde: String? = null, hasta: String? = null): List<TransitoPorAcceso>? = try {
        val respuesta = cliente.medicion.accesosDelTipo(autorizacion, tipoDia, desde, hasta)
        if (respuesta.isSuccessful) respuesta.body()?.map { it.aTransito() } else null
    } catch (e: Exception) {
        null
    }

    suspend fun reporte(fecha: String): ReporteRemoto? = try {
        val respuesta = cliente.medicion.reporte(autorizacion, fecha)
        if (respuesta.isSuccessful) respuesta.body() else null
    } catch (e: Exception) {
        null
    }

    private fun AccesoMedidoRemoto.aTransito() = TransitoPorAcceso(
        acceso = "A" + idAcceso,
        ingresos = ingresos.toInt(),

        transitoEstimado = transito?.toDouble(),
        origen = OrigenTransito.porNombre(origen)
    )

    private fun IndicadorRemoto.aIndicadores(desdePedido: String, hastaPedido: String) = IndicadoresDia(
        fecha = desde ?: desdePedido,
        tipoDia = tipoDia ?: "Habil",
        ingresos = (ingresos ?: 0L).toInt(),
        transacciones = (transacciones ?: 0L).toInt(),
        conversionBruta = conversionBruta,
        estadoDia = dia ?: "Sin información",
        transito = transito,

        flujoSinTransito = transito?.let { (ingresos ?: 0L) - it }?.takeIf { transito != null && transito!! > 0 },
        conversionSinTransito = null,
        bloquesMedidos = medidos ?: 0,
        bloquesEstimados = 0,
        bloquesSinInformacion = sinInformacion ?: 0,
        dias = maxOf(1, diasEntre(desde ?: desdePedido, hasta ?: hastaPedido)),
        desde = desde ?: desdePedido,
        hasta = hasta ?: hastaPedido
    )

    private fun diasEntre(desde: String, hasta: String): Int = try {
        val a = java.time.LocalDate.parse(desde)
        val b = java.time.LocalDate.parse(hasta)
        (java.time.temporal.ChronoUnit.DAYS.between(a, b) + 1).toInt()
    } catch (e: Exception) {
        1
    }
}
