package com.example.rinconsur.data.exportacion

import android.content.Context
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.RepositorioIndicadoresRemoto
import com.example.rinconsur.data.remoto.TokenPreferencias
import com.example.rinconsur.data.remoto.aReporteJefatura
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.ReporteJefatura
import com.example.rinconsur.model.TransitoPorAcceso
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExportadorMetricas(private val contexto: Context) {

    private val repositorio = FlujoRepository(contexto)
    private val tokens = TokenPreferencias(contexto)
    private val direccion = DireccionServidor(contexto)

    private class Datos(
        val reporte: ReporteJefatura,
        val habiles: IndicadoresDia?,
        val accesos: List<TransitoPorAcceso>
    )

    fun exportar(): File {
        val hoy = repositorio.fechaHoy()
        val datos = delServicio(hoy) ?: deLaCopiaLocal(hoy)
        val reporte = datos.reporte
        val habiles = datos.habiles
        val accesos = datos.accesos

        val libro = LibroExcel()
            .hoja("Resumen", resumen(hoy, reporte, habiles))
            .hoja("Meses", meses(reporte))
            .hoja("Horas punta", horas(reporte))
            .hoja("Perfil", perfil(reporte))
            .hoja("Accesos", filasAccesos(accesos))

        val carpeta = File(contexto.cacheDir, "exportaciones").apply { mkdirs() }
        val nombre = "rincon-sur-metricas-" + SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) + ".xlsx"
        val destino = File(carpeta, nombre)
        libro.guardar(destino)
        return destino
    }

    private fun delServicio(hoy: String): Datos? {
        val token = tokens.leer() ?: return null
        return runCatching {
            runBlocking {
                val remoto = RepositorioIndicadoresRemoto(direccion.leer(), token)
                val reporte = remoto.reporte(hoy) ?: return@runBlocking null
                val periodo = remoto.delPeriodo("Habil")
                val accesos = remoto.accesosDelPeriodo("Habil").orEmpty()
                Datos(reporte.aReporteJefatura(repositorio.bloques()), periodo, accesos)
            }
        }.getOrNull()
    }

    private fun deLaCopiaLocal(hoy: String) = Datos(
        repositorio.reporteJefatura(),
        repositorio.indicadoresDeTipoDia("Habil"),
        repositorio.transitoPorAcceso(hoy).ifEmpty { repositorio.transitoPorAccesoDeTipo("Habil") }
    )

    private fun resumen(hoy: String, reporte: ReporteJefatura, habiles: IndicadoresDia?): List<List<Any?>> {
        val indicador = habiles
        val meta = reporte.meta
        return listOf(
            listOf("Métrica", "Valor", "Detalle"),
            listOf("Período", meta.mes.toString() + "/" + meta.anio.toString(), "Reporte al " + hoy),
            listOf("Meta de conversión", textoPorcentaje(meta.objetivo), if (meta.registrada) "registrada" else "sin registrar"),
            listOf("Conversión medida", textoPorcentaje(meta.actual), "boletas sobre ingresos"),
            listOf("Cumplimiento de la meta", textoPorcentaje(meta.cumplimiento), "sobre la meta del mes"),
            listOf("Días con datos", meta.dias, "días con ventas cargadas"),
            listOf("Ingresos del período", indicador?.ingresos ?: 0, "personas que entraron"),
            listOf("Transacciones del período", indicador?.transacciones ?: 0, "boletas emitidas"),
            listOf("Conversión del período", textoPorcentaje(indicador?.conversionBruta), "calculada por el servicio"),
            listOf("Tránsito estimado", indicador?.transito ?: 0, "personas que pasaron de largo"),
            listOf("Bloques medidos", indicador?.bloquesMedidos ?: 0, "de los bloques con venta"),
            listOf("Bloques sin información", indicador?.bloquesSinInformacion ?: 0, "sin sesión validada")
        )
    }

    private fun meses(reporte: ReporteJefatura): List<List<Any?>> {
        val filas = mutableListOf<List<Any?>>(listOf("Mes", "Ingresos", "Boletas", "Conversión %", "Días con datos"))
        reporte.meses.forEach { mes ->
            filas.add(listOf(mes.periodo, mes.ingresos, mes.boletas, mes.conversion ?: "", mes.dias))
        }
        return filas
    }

    private fun horas(reporte: ReporteJefatura): List<List<Any?>> {
        val filas = mutableListOf<List<Any?>>(listOf("Bloque", "Ingresos", "Tránsito medido", "Medido", "Bloques"))
        reporte.horas.forEach { hora ->
            filas.add(listOf(hora.hora, hora.ingresos, hora.transito, if (hora.medido) "sí" else "no", hora.bloques))
        }
        return filas
    }

    private fun perfil(reporte: ReporteJefatura): List<List<Any?>> {
        val filas = mutableListOf<List<Any?>>(listOf("Categoría", "Grupo", "Personas", "Porcentaje %"))
        reporte.porRango.forEach { filas.add(listOf("Rango etario", it.etiqueta, it.cantidad, it.porcentaje ?: "")) }
        reporte.porCompania.forEach { filas.add(listOf("Compañía", it.etiqueta, it.cantidad, it.porcentaje ?: "")) }
        reporte.compras.forEach { filas.add(listOf("Compras en otra tienda", it.etiqueta, it.cantidad, it.porcentaje ?: "")) }
        return filas
    }

    private fun filasAccesos(accesos: List<com.example.rinconsur.model.TransitoPorAcceso>): List<List<Any?>> {
        val filas = mutableListOf<List<Any?>>(listOf("Acceso", "Ingresos", "Tránsito", "Origen del tránsito"))
        accesos.forEach { acceso ->
            filas.add(listOf(acceso.acceso, acceso.ingresos, acceso.transitoEstimado ?: "", acceso.origen.etiqueta))
        }
        return filas
    }

    private fun textoPorcentaje(valor: Double?): String =
        valor?.let { String.format(Locale("es", "CL"), "%.1f %%", it) } ?: "sin dato"
}
