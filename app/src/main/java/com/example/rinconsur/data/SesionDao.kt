package com.example.rinconsur.data

import android.content.ContentValues
import android.database.Cursor
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.DetalleSesion
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.Registro
import com.example.rinconsur.model.ResumenSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.model.Clasificacion

internal class SesionDao(private val helper: FlujoDbHelper) {

    fun sesionEnCursoDe(idObservador: Long): SesionResumen? =
        consultarSesiones(FlujoDbHelper.SQL_SESIONES + "WHERE s.id_observador = ? AND s.id_estado = ?", arrayOf(idObservador.toString(), FlujoDbHelper.ESTADO_EN_CURSO.toString())).firstOrNull()

    fun sesionesDeFecha(fecha: String): List<SesionResumen> =
        consultarSesiones(FlujoDbHelper.SQL_SESIONES + "WHERE s.fecha = ? ORDER BY s.hora_inicio_prog", arrayOf(fecha))

    fun buscarSesion(idSesion: Long): SesionResumen? =
        consultarSesiones(FlujoDbHelper.SQL_SESIONES + "WHERE s.id_sesion = ?", arrayOf(idSesion.toString())).firstOrNull()

    fun programarSesion(
        idAcceso: Long,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        idObservador: Long,
        idProgramador: Long
    ): String {
        val siguiente = helper.readableDatabase
            .rawQuery("SELECT COALESCE(MAX(id_sesion), 0) + 1 AS siguiente FROM sesion_observacion", null)
            .use { c -> if (c.moveToNext()) c.long("siguiente") else 1L }
        val codigo = "S-" + siguiente.toString().padStart(4, '0')
        val valores = ContentValues().apply {
            put("id_sesion", siguiente)
            put("codigo", codigo)
            put("id_acceso", idAcceso)
            put("fecha", fecha)
            put("hora_inicio_prog", horaInicio)
            put("hora_fin_prog", horaFin)
            put("id_estado", FlujoDbHelper.ESTADO_PROGRAMADA)
            put("id_observador", idObservador)
            put("id_programador", idProgramador)
        }
        helper.writableDatabase.insert("sesion_observacion", null, valores)
        return codigo
    }

    fun sesionesPorValidarTodas(): List<SesionResumen> =
        consultarSesiones(FlujoDbHelper.SQL_SESIONES + "WHERE s.id_estado = ? ORDER BY s.fecha DESC, s.hora_inicio_prog",
            arrayOf(FlujoDbHelper.ESTADO_FINALIZADA.toString()))

    fun sesionesDeLosUltimosDias(desde: String, dias: Int): List<SesionResumen> =
        consultarSesiones(
            FlujoDbHelper.SQL_SESIONES + "WHERE s.fecha <= ? AND s.fecha >= date(?, ?) ORDER BY s.fecha DESC, s.hora_inicio_prog",
            arrayOf(desde, desde, "-" + dias + " day")
        )

    fun sesionesPorValidar(fecha: String): List<SesionResumen> =
        consultarSesiones(FlujoDbHelper.SQL_SESIONES + "WHERE s.fecha = ? AND s.id_estado = ?", arrayOf(fecha, FlujoDbHelper.ESTADO_FINALIZADA.toString()))

    fun resumenDeSesion(idSesion: Long): ResumenSesion {
        val sesion = consultarSesiones(FlujoDbHelper.SQL_SESIONES + "WHERE s.id_sesion = ?", arrayOf(idSesion.toString())).firstOrNull()
            ?: return ResumenSesion(codigo = "")
        return ResumenSesion(
            codigo = sesion.codigo,
            transito = sesion.transito,
            visita = sesion.visita,
            noDeterminado = sesion.noDeterminado
        )
    }

    fun iniciarSesion(idSesion: Long, hora: String): Int =
        helper.writableDatabase.update(
            "sesion_observacion",
            ContentValues().apply {
                put("inicio_real", hora)
                put("id_estado", FlujoDbHelper.ESTADO_EN_CURSO)
            },
            "id_sesion = ?", arrayOf(idSesion.toString())
        )

    fun cerrarSesionReal(idSesion: Long, hora: String): Int =
        helper.writableDatabase.update(
            "sesion_observacion",
            ContentValues().apply {
                put("fin_real", hora)
                put("id_estado", FlujoDbHelper.ESTADO_FINALIZADA)
            },
            "id_sesion = ? AND id_estado = ?",
            arrayOf(idSesion.toString(), FlujoDbHelper.ESTADO_EN_CURSO.toString())
        )

    fun revisarSesion(idSesion: Long, estado: EstadoSesion, idRevisor: Long, motivo: String? = null): Int =
        helper.writableDatabase.update(
            "sesion_observacion",
            ContentValues().apply {
                put("id_estado", estado.idEstado)
                put("id_revisor", idRevisor)
                put("fecha_revision", ahora())
                put("motivo_rechazo", motivo)
            },
            "id_sesion = ?", arrayOf(idSesion.toString())
        )

    fun insertarRegistro(
        idSesion: Long,
        clasificacion: Clasificacion,
        rangoEtario: RangoEtario,
        compania: Compania,
        comprasOtraTienda: Boolean,
        seDirige: Long? = null
    ): Long {

        val estadoActual = helper.writableDatabase.rawQuery(
            "SELECT id_estado FROM sesion_observacion WHERE id_sesion = ?",
            arrayOf(idSesion.toString())
        ).use { c -> if (c.moveToFirst()) c.getLong(0) else -1L }
        if (estadoActual != FlujoDbHelper.ESTADO_EN_CURSO) return -1L
        val valores = ContentValues().apply {
            put("id_sesion", idSesion)
            put("fecha_hora", ahora())
            put("id_clasificacion", clasificacion.idClasificacion)
            put("id_rango", rangoEtario.idRango)
            put("id_tipo_compania", compania.idTipo)
            put("compras_otra_tienda", if (comprasOtraTienda) 1 else 0)
            put("id_acceso_destino", seDirige)
            put("anulado", 0)
        }
        return helper.writableDatabase.insert("registro_observacion", null, valores)
    }

    fun anularRegistro(idRegistro: Long, motivo: String, idColaborador: Long): Int =
        helper.writableDatabase.update(
            "registro_observacion",
            ContentValues().apply {
                put("anulado", 1)
                put("motivo_anulacion", motivo)
                put("id_usuario_anula", idColaborador)
                put("fecha_anulacion", ahora())
            },
            "id_registro = ?", arrayOf(idRegistro.toString())
        )

    fun ultimosRegistros(idSesion: Long, limite: Int = 8): List<Registro> {
        val lista = mutableListOf<Registro>()
        helper.readableDatabase.rawQuery(
            "SELECT r.*, c.nombre AS clasificacion_nombre, s.codigo AS codigo_sesion FROM registro_observacion r " +
                "JOIN clasificacion c ON c.id_clasificacion = r.id_clasificacion " +
                "JOIN sesion_observacion s ON s.id_sesion = r.id_sesion " +
                "WHERE r.id_sesion = ? ORDER BY r.id_registro DESC LIMIT ?",
            arrayOf(idSesion.toString(), limite.toString())
        ).use { c ->
            while (c.moveToNext()) lista.add(leerRegistro(c))
        }
        return lista
    }

    fun detalleDeSesion(idSesion: Long): DetalleSesion? {
        val resumen = buscarSesion(idSesion) ?: return null
        val db = helper.readableDatabase
        val args = arrayOf(idSesion.toString())

        var validos = 0
        var anulados = 0
        var primera: String? = null
        var ultima: String? = null
        db.rawQuery(
            "SELECT SUM(CASE WHEN anulado = 0 THEN 1 ELSE 0 END) AS validos, " +
                "SUM(CASE WHEN anulado = 1 THEN 1 ELSE 0 END) AS anulados, " +
                "MIN(CASE WHEN anulado = 0 THEN fecha_hora END) AS primera, " +
                "MAX(CASE WHEN anulado = 0 THEN fecha_hora END) AS ultima " +
                "FROM registro_observacion WHERE id_sesion = ?",
            args
        ).use { c ->
            if (c.moveToNext()) {
                validos = c.entero("validos")
                anulados = c.entero("anulados")
                primera = c.textoOpcional("primera")
                ultima = c.textoOpcional("ultima")
            }
        }
        if (validos == 0) {
            return DetalleSesion(resumen, validos, anulados, primera, ultima)
        }

        // Las claves van en este orden: primero el total para el porcentaje, después la sesión.
        fun grupos(select: String, join: String, extra: String, grupo: String, orden: String): List<GrupoConteo> {
            val sql = "SELECT " + select + ", COUNT(*) AS cantidad, " +
                "ROUND(100.0 * COUNT(*) / NULLIF(?, 0), 1) AS porcentaje " +
                "FROM registro_observacion r " + join +
                " WHERE r.id_sesion = ? AND r.anulado = 0 " + extra +
                " GROUP BY " + grupo + " ORDER BY " + orden
            val lista = mutableListOf<GrupoConteo>()
            db.rawQuery(sql, arrayOf(validos.toString(), idSesion.toString())).use { c ->
                while (c.moveToNext()) {
                    lista.add(GrupoConteo(c.texto("etiqueta"), c.entero("cantidad"), c.decimalOpcional("porcentaje")))
                }
            }
            return lista
        }

        return DetalleSesion(
            sesion = resumen,
            registrosValidos = validos,
            anulados = anulados,
            primeraMarca = primera,
            ultimaMarca = ultima,
            porClasificacion = grupos(
                "c.nombre AS etiqueta",
                "JOIN clasificacion c ON c.id_clasificacion = r.id_clasificacion",
                "", "c.nombre", "COUNT(*) DESC"
            ),
            porRango = grupos(
                "rg.nombre AS etiqueta",
                "JOIN rango_etario rg ON rg.id_rango = r.id_rango",
                "", "rg.nombre, rg.orden", "rg.orden"
            ),
            porCompania = grupos(
                "tc.nombre AS etiqueta",
                "JOIN tipo_compania tc ON tc.id_tipo_compania = r.id_tipo_compania",
                "", "tc.nombre", "COUNT(*) DESC"
            ),
            comprasOtraTienda = grupos(
                "CASE WHEN r.compras_otra_tienda = 1 THEN 'Compró en otra tienda' " +
                    "ELSE 'No compró en otra tienda' END AS etiqueta",
                "", "", "r.compras_otra_tienda", "r.compras_otra_tienda DESC"
            ),
            destinosTransito = grupos(
                "a.codigo AS etiqueta",
                "JOIN acceso a ON a.id_acceso = r.id_acceso_destino",
                "AND r.id_clasificacion = 1", "a.codigo", "COUNT(*) DESC"
            )
        )
    }

    private fun consultarSesiones(sql: String, args: Array<String>?): List<SesionResumen> {
        val lista = mutableListOf<SesionResumen>()
        helper.readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    SesionResumen(
                        id = c.long("id_sesion"),
                        codigo = c.texto("codigo"),
                        idAcceso = c.long("id_acceso"),
                        fecha = c.texto("fecha"),
                        horaInicioProg = c.texto("hora_inicio_prog"),
                        horaFinProg = c.texto("hora_fin_prog"),
                        inicioReal = c.textoOpcional("inicio_real"),
                        finReal = c.textoOpcional("fin_real"),
                        estado = EstadoSesion.porNombre(c.texto("estado_nombre")),
                        idObservador = c.long("id_observador"),
                        idProgramador = c.long("id_programador"),
                        idRevisor = c.longOpcional("id_revisor"),
                        fechaRevision = c.textoOpcional("fecha_revision"),
                        motivoRechazo = c.textoOpcional("motivo_rechazo"),
                        nombreObservador = c.texto("nombre_observador"),
                        registrosValidos = c.entero("registros_validos"),
                        transito = c.entero("transito"),
                        visita = c.entero("visita"),
                        noDeterminado = c.entero("no_determinado"),
                        pctTransito = c.decimalOpcional("pct_transito")
                    )
                )
            }
        }
        return lista
    }

    private fun leerRegistro(c: Cursor) = Registro(
        id = c.long("id_registro"),
        idSesion = c.long("id_sesion"),
        codigoSesion = c.texto("codigo_sesion"),
        fechaHora = c.texto("fecha_hora"),
        hora = c.texto("fecha_hora").takeLast(8).take(5),
        clasificacion = Clasificacion.porNombre(c.texto("clasificacion_nombre")),
        rangoEtario = RangoEtario.porId(c.long("id_rango")),
        compania = Compania.porId(c.long("id_tipo_compania")),
        comprasOtraTienda = c.entero("compras_otra_tienda") == 1,
        seDirige = c.longOpcional("id_acceso_destino"),
        anulado = c.entero("anulado") == 1,
        idUsuarioAnula = c.longOpcional("id_usuario_anula"),
        fechaAnulacion = c.textoOpcional("fecha_anulacion"),
        motivoAnulacion = c.textoOpcional("motivo_anulacion")
    )
}
