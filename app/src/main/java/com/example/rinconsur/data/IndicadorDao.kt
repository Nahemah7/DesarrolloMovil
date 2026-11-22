package com.example.rinconsur.data

import android.content.ContentValues
import com.example.rinconsur.model.ConversionMes
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.model.HoraPunta
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.MedicionPeriodo
import com.example.rinconsur.model.MetaDelMes
import com.example.rinconsur.model.ReporteJefatura
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

    fun medicionDeFecha(fecha: String): MedicionPeriodo =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) AS personas, COUNT(DISTINCT s.id_acceso || '#' || s.hora_inicio_prog) AS bloques, " +
                "1 AS dias, COUNT(DISTINCT CASE WHEN s.id_estado = 4 THEN s.id_sesion END) AS validadas " +
                "FROM registro_observacion r JOIN sesion_observacion s ON s.id_sesion = r.id_sesion " +
                "WHERE s.fecha = ? AND r.anulado = 0 AND s.id_estado IN (3, 4)",
            arrayOf(fecha)
        ).use { c -> leerMedicion(c) }

    fun medicionDeTipo(tipo: String, desde: String? = null, hasta: String? = null): MedicionPeriodo =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) AS personas, COUNT(DISTINCT s.id_acceso || '#' || s.hora_inicio_prog) AS bloques, " +
                "COUNT(DISTINCT s.fecha) AS dias, COUNT(DISTINCT CASE WHEN s.id_estado = 4 THEN s.id_sesion END) AS validadas " +
                "FROM registro_observacion r JOIN sesion_observacion s ON s.id_sesion = r.id_sesion " +
                "JOIN v_tipo_dia td ON td.fecha = s.fecha " +
                "WHERE td.tipo_dia = ? AND r.anulado = 0 AND s.id_estado IN (3, 4)" +
                (if (desde != null && hasta != null) " AND s.fecha BETWEEN ? AND ?" else ""),
            (if (desde != null && hasta != null) arrayOf(tipo, desde, hasta) else arrayOf(tipo))
        ).use { c -> leerMedicion(c) }

    private fun leerMedicion(c: android.database.Cursor): MedicionPeriodo {

        return if (c.moveToNext()) {
            MedicionPeriodo(
                personas = c.entero("personas"),
                bloques = c.entero("bloques"),
                dias = c.entero("dias"),
                sesionesValidadas = c.entero("validadas")
            )
        } else MedicionPeriodo()
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

    fun reporteJefatura(hoy: String): ReporteJefatura {
        val db = helper.readableDatabase
        val periodo = hoy.take(7)

        var objetivo: Double? = null
        db.rawQuery(
            "SELECT porcentaje FROM meta_conversion WHERE substr(?, 1, 4) = anio AND substr(?, 6, 2) = mes",
            arrayOf(periodo, periodo)
        ).use { c -> if (c.moveToNext()) objetivo = c.decimalOpcional("porcentaje") }

        var actual: Double? = null
        var dias = 0
        db.rawQuery(
            "SELECT COUNT(*) AS dias, ROUND(100.0 * SUM(transacciones) / NULLIF(SUM(ingresos), 0), 1) AS conversion " +
                "FROM v_conversion_dia WHERE substr(fecha, 1, 7) = ?",
            arrayOf(periodo)
        ).use { c ->
            if (c.moveToNext()) {
                dias = c.entero("dias")
                actual = c.decimalOpcional("conversion")
            }
        }

        val meses = mutableListOf<ConversionMes>()
        db.rawQuery(
            "SELECT substr(fecha, 1, 7) AS mes, SUM(ingresos) AS ingresos, SUM(transacciones) AS boletas, " +
                "ROUND(100.0 * SUM(transacciones) / NULLIF(SUM(ingresos), 0), 1) AS conversion, " +
                "COUNT(*) AS dias FROM v_conversion_dia GROUP BY substr(fecha, 1, 7) ORDER BY mes DESC LIMIT 6",
            arrayOf()
        ).use { c ->
            while (c.moveToNext()) {
                meses.add(
                    ConversionMes(
                        mes = c.texto("mes"),
                        ingresos = c.entero("ingresos"),
                        boletas = c.entero("boletas"),
                        conversion = c.decimalOpcional("conversion"),
                        dias = c.entero("dias")
                    )
                )
            }
        }

        val horas = mutableListOf<HoraPunta>()
        db.rawQuery(
            "SELECT hora_inicio AS hora, SUM(ingresos) AS ingresos, SUM(transito_estimado) AS transito, " +
                "SUM(CASE WHEN origen = 'medido' THEN 1 ELSE 0 END) AS medidos, COUNT(*) AS bloques " +
                "FROM v_transito_bloque GROUP BY hora_inicio ORDER BY hora_inicio",
            arrayOf()
        ).use { c ->
            while (c.moveToNext()) {
                horas.add(
                    HoraPunta(
                        hora = c.texto("hora").take(5),
                        ingresos = c.entero("ingresos"),
                        transito = c.entero("transito"),
                        medidos = c.entero("medidos"),
                        bloques = c.entero("bloques")
                    )
                )
            }
        }

        fun grupos(columna: String, orden: String): List<GrupoConteo> {
            val lista = mutableListOf<GrupoConteo>()
            db.rawQuery(
                "SELECT " + columna + " AS etiqueta, SUM(personas) AS cantidad, " +
                    "ROUND(100.0 * SUM(personas) / NULLIF((SELECT SUM(personas) FROM v_perfil_transito), 0), 1) AS porcentaje " +
                    "FROM v_perfil_transito GROUP BY " + columna + " ORDER BY " + orden,
                arrayOf()
            ).use { c ->
                while (c.moveToNext()) {
                    lista.add(GrupoConteo(c.texto("etiqueta"), c.entero("cantidad"), c.decimalOpcional("porcentaje")))
                }
            }
            return lista
        }

        return ReporteJefatura(
            deEjemplo = helper.readableDatabase.rawQuery(
                "SELECT valor FROM configuracion WHERE clave = 'datos_ejemplo'", arrayOf()
            ).use { c -> c.moveToFirst() && c.getString(0) == "1" },
            meta = MetaDelMes(
                anio = periodo.take(4).toIntOrNull() ?: 0,
                mes = periodo.takeLast(2).toIntOrNull() ?: 0,
                objetivo = objetivo,
                actual = actual,
                dias = dias
            ),
            meses = meses,
            horas = horas,
            porRango = grupos("rango_etario", "cantidad DESC"),
            porCompania = grupos("compania", "cantidad DESC"),
            compras = grupos(
                "CASE WHEN compras_otra_tienda = 1 THEN 'Compró en otra tienda' ELSE 'No compró en otra tienda' END",
                "compras_otra_tienda DESC"
            )
        )
    }

    fun guardarMeta(anio: Int, mes: Int, porcentaje: Double, idUsuario: Long): Boolean {
        val valores = ContentValues().apply {
            put("anio", anio)
            put("mes", mes)
            put("porcentaje", porcentaje)
            put("id_usuario_registra", idUsuario)
        }
        return helper.writableDatabase.insertWithOnConflict(
            "meta_conversion", null, valores, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
        ) > 0
    }
}
