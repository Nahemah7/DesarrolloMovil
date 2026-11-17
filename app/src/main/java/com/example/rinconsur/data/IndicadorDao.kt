package com.example.rinconsur.data

import android.content.ContentValues
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.TransitoPorAcceso
import com.example.rinconsur.model.OrigenTransito

internal class IndicadorDao(private val helper: FlujoDbHelper) {

    fun indicadoresDeFecha(fecha: String): IndicadoresDia? =
        helper.readableDatabase.rawQuery("SELECT * FROM v_conversion_dia WHERE fecha = ?", arrayOf(fecha)).use { c ->
            if (!c.moveToNext()) return null
            IndicadoresDia(
                fecha = c.texto("fecha"),
                tipoDia = c.texto("tipo_dia"),
                ingresos = c.entero("ingresos"),
                transacciones = c.entero("transacciones"),
                conversionBruta = c.decimalOpcional("conversion_bruta"),
                estadoDia = c.texto("dia"),
                transito = c.decimalOpcional("transito"),
                flujoSinTransito = c.decimalOpcional("flujo_sin_transito"),
                conversionSinTransito = c.decimalOpcional("conversion_sin_transito"),
                bloquesMedidos = c.entero("medidos"),
                bloquesEstimados = c.entero("estimados"),
                bloquesSinInformacion = c.entero("sin_informacion")
            )
        }

    fun indicadoresDeTipoDia(tipo: String, desde: String? = null, hasta: String? = null): IndicadoresDia? =
        helper.readableDatabase.rawQuery(
            "SELECT MIN(fecha) AS desde, MAX(fecha) AS hasta, COUNT(*) AS dias, tipo_dia, " +
                "SUM(ingresos) AS ingresos, SUM(transacciones) AS transacciones, " +
                "ROUND(100.0 * SUM(transacciones) / NULLIF(SUM(ingresos), 0), 1) AS conversion_bruta, " +
                "CASE WHEN SUM(CASE WHEN dia = 'completo' THEN 1 ELSE 0 END) = COUNT(*) " +
                "     THEN 'completo' ELSE 'incompleto' END AS dia, " +
                "SUM(transito) AS transito, SUM(ingresos) - SUM(transito) AS flujo_sin_transito, " +
                "CASE WHEN SUM(sin_informacion) = 0 " +
                "     THEN ROUND(100.0 * SUM(transacciones) / NULLIF(SUM(ingresos) - SUM(transito), 0), 1) " +
                "     END AS conversion_sin_transito, " +
                "SUM(medidos) AS medidos, SUM(estimados) AS estimados, SUM(sin_informacion) AS sin_informacion " +
                "FROM v_conversion_dia WHERE tipo_dia = ?" +
                (if (desde != null && hasta != null) " AND fecha BETWEEN ? AND ?" else ""),
            (if (desde != null && hasta != null) arrayOf(tipo, desde, hasta) else arrayOf(tipo))
        ).use { c ->
            if (!c.moveToNext() || c.getInt(c.getColumnIndexOrThrow("dias")) == 0) return null
            IndicadoresDia(
                fecha = c.texto("hasta"),
                tipoDia = c.texto("tipo_dia"),
                ingresos = c.entero("ingresos"),
                transacciones = c.entero("transacciones"),
                conversionBruta = c.decimalOpcional("conversion_bruta"),
                estadoDia = c.texto("dia"),
                transito = c.decimalOpcional("transito"),
                flujoSinTransito = c.decimalOpcional("flujo_sin_transito"),
                conversionSinTransito = c.decimalOpcional("conversion_sin_transito"),
                bloquesMedidos = c.entero("medidos"),
                bloquesEstimados = c.entero("estimados"),
                bloquesSinInformacion = c.entero("sin_informacion"),
                dias = c.entero("dias"),
                desde = c.texto("desde"),
                hasta = c.texto("hasta")
            )
        }

    fun semanasConDatos(): List<Pair<String, String>> {
        val lista = mutableListOf<Pair<String, String>>()
        helper.readableDatabase.rawQuery(
            "SELECT date(fecha, '-' || ((CAST(strftime('%w', fecha) AS INTEGER) + 6) % 7) || ' days') AS desde, " +
                "       date(fecha, '-' || ((CAST(strftime('%w', fecha) AS INTEGER) + 6) % 7) || ' days', '+6 days') AS hasta " +
                "FROM v_conversion_dia GROUP BY desde ORDER BY desde DESC",
            null
        ).use { c ->
            while (c.moveToNext()) lista.add(c.texto("desde") to c.texto("hasta"))
        }
        return lista
    }

    fun transitoPorAccesoDeTipo(tipo: String, desde: String? = null, hasta: String? = null): List<TransitoPorAcceso> {
        val lista = mutableListOf<TransitoPorAcceso>()
        helper.readableDatabase.rawQuery(
            "SELECT acceso, SUM(ingresos) AS ingresos, SUM(transito_estimado) AS transito, " +
                "CASE WHEN SUM(CASE WHEN origen = 'medido' THEN 1 ELSE 0 END) > 0 THEN 'medido' " +
                "     WHEN SUM(CASE WHEN origen = 'estimado' THEN 1 ELSE 0 END) > 0 THEN 'estimado' " +
                "     ELSE 'sin información' END AS origen " +
                "FROM v_transito_bloque WHERE tipo_dia = ?" +
                (if (desde != null && hasta != null) " AND fecha BETWEEN ? AND ?" else "") +
                " GROUP BY acceso ORDER BY acceso",
            (if (desde != null && hasta != null) arrayOf(tipo, desde, hasta) else arrayOf(tipo))
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    TransitoPorAcceso(
                        acceso = c.texto("acceso"),
                        ingresos = c.entero("ingresos"),
                        transitoEstimado = c.decimalOpcional("transito"),
                        origen = OrigenTransito.porNombre(c.texto("origen"))
                    )
                )
            }
        }
        return lista
    }

    fun transitoPorAcceso(fecha: String): List<TransitoPorAcceso> {
        val lista = mutableListOf<TransitoPorAcceso>()
        helper.readableDatabase.rawQuery(
            "SELECT acceso, SUM(ingresos) AS ingresos, SUM(transito_estimado) AS transito, " +
                "CASE WHEN SUM(CASE WHEN origen = 'medido' THEN 1 ELSE 0 END) > 0 THEN 'medido' " +
                "     WHEN SUM(CASE WHEN origen = 'estimado' THEN 1 ELSE 0 END) > 0 THEN 'estimado' " +
                "     ELSE 'sin información' END AS origen " +
                "FROM v_transito_bloque WHERE fecha = ? GROUP BY acceso ORDER BY acceso",
            arrayOf(fecha)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    TransitoPorAcceso(
                        acceso = c.texto("acceso"),
                        ingresos = c.entero("ingresos"),
                        transitoEstimado = c.decimalOpcional("transito"),
                        origen = OrigenTransito.porNombre(c.texto("origen"))
                    )
                )
            }
        }
        return lista
    }

    fun bloquesPendientes(fecha: String): List<String> {
        val lista = mutableListOf<String>()
        helper.readableDatabase.rawQuery(
            "SELECT acceso, hora_inicio, fecha FROM v_pendiente_medir WHERE fecha = ? ORDER BY hora_inicio",
            arrayOf(fecha)
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(c.texto("acceso") + " · " + c.texto("hora_inicio").take(5) + " · " + c.texto("fecha"))
            }
        }
        return lista
    }
}
