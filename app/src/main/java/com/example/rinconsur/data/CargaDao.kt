package com.example.rinconsur.data

import android.content.ContentValues
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.BloqueCarga
import com.example.rinconsur.model.CargaDia

internal class CargaDao(private val helper: FlujoDbHelper) {

    fun cargaDeFecha(fecha: String): CargaDia {
        val db = helper.readableDatabase
        val accesos = mutableListOf<Acceso>()
        db.rawQuery(
            "SELECT id_acceso, codigo, nombre, ubicacion, observacion FROM acceso ORDER BY id_acceso",
            arrayOf()
        ).use { c ->
            while (c.moveToNext()) {
                accesos.add(
                    Acceso(
                        id = c.getLong(0),
                        codigo = c.getString(1),
                        nombre = c.getString(2),
                        ubicacion = c.getString(3),
                        observacion = c.getString(4)
                    )
                )
            }
        }

        val detalle = mutableMapOf<Long, MutableMap<Long, String>>()
        db.rawQuery(
            "SELECT id_bloque, id_acceso, cantidad FROM conteo_ingreso " +
                "WHERE fecha = ? AND id_acceso IS NOT NULL",
            arrayOf(fecha)
        ).use { c ->
            while (c.moveToNext()) {
                detalle.getOrPut(c.getLong(0)) { mutableMapOf() }[c.getLong(1)] = c.getInt(2).toString()
            }
        }

        val bloques = mutableListOf<BloqueCarga>()
        db.rawQuery(
            "SELECT bh.id_bloque, bh.hora_inicio, " +
                "(SELECT SUM(cantidad) FROM conteo_ingreso WHERE fecha = ? AND id_bloque = bh.id_bloque) AS ingresos, " +
                "(SELECT COUNT(*) FROM transaccion_bloque WHERE fecha = ? AND id_bloque = bh.id_bloque) AS hay_boletas, " +
                "(SELECT SUM(cantidad) FROM transaccion_bloque WHERE fecha = ? AND id_bloque = bh.id_bloque) AS boletas " +
                "FROM bloque_horario bh ORDER BY bh.id_bloque",
            arrayOf(fecha, fecha, fecha)
        ).use { c ->
            while (c.moveToNext()) {
                val idBloque = c.getLong(0)
                val ingresos = c.decimalOpcional("ingresos")
                val boletas = c.decimalOpcional("boletas")
                bloques.add(
                    BloqueCarga(
                        idBloque = idBloque,
                        hora = c.texto("hora_inicio").take(5),
                        ingresos = ingresos?.toInt()?.toString() ?: "",
                        boletas = if (c.entero("hay_boletas") > 0) (boletas?.toInt()?.toString() ?: "0") else "",
                        detalle = detalle[idBloque] ?: emptyMap(),
                        detallado = detalle.containsKey(idBloque)
                    )
                )
            }
        }

        return CargaDia(fecha = fecha, accesos = accesos, bloques = bloques)
    }

    fun guardar(carga: CargaDia, idUsuario: Long, momento: String): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            carga.bloques.forEach { bloque ->
                val habia = habiaDatos(carga.fecha, bloque.idBloque)
                db.delete("conteo_ingreso", "fecha = ? AND id_bloque = ?",
                    arrayOf(carga.fecha, bloque.idBloque.toString()))
                db.delete("transaccion_bloque", "fecha = ? AND id_bloque = ?",
                    arrayOf(carga.fecha, bloque.idBloque.toString()))

                if (bloque.detallado) {
                    bloque.detalle.forEach { (idAcceso, texto) ->
                        val valor = texto.trim().toIntOrNull() ?: return@forEach
                        insertarConteo(carga.fecha, bloque.idBloque, idAcceso, valor, idUsuario, if (habia) momento else null)
                    }
                } else {
                    bloque.ingresosNumero?.let { valor ->
                        insertarConteo(carga.fecha, bloque.idBloque, null, valor, idUsuario, if (habia) momento else null)
                    }
                }

                bloque.boletasNumero?.let { valor ->
                    val valores = ContentValues().apply {
                        put("fecha", carga.fecha)
                        put("id_bloque", bloque.idBloque)
                        put("cantidad", valor)
                        put("id_usuario_carga", idUsuario)
                        if (habia) {
                            put("id_usuario_correccion", idUsuario)
                            put("fecha_correccion", momento)
                        }
                    }
                    db.insert("transaccion_bloque", null, valores)
                }
            }
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }

    private fun habiaDatos(fecha: String, idBloque: Long): Boolean =
        helper.readableDatabase.rawQuery(
            "SELECT (SELECT COUNT(*) FROM conteo_ingreso WHERE fecha = ? AND id_bloque = ?) + " +
                "(SELECT COUNT(*) FROM transaccion_bloque WHERE fecha = ? AND id_bloque = ?) AS filas",
            arrayOf(fecha, idBloque.toString(), fecha, idBloque.toString())
        ).use { c -> c.moveToFirst() && c.getInt(0) > 0 }

    private fun insertarConteo(
        fecha: String,
        idBloque: Long,
        idAcceso: Long?,
        cantidad: Int,
        idUsuario: Long,
        momentoCorreccion: String?
    ) {
        val valores = ContentValues().apply {
            put("fecha", fecha)
            put("id_bloque", idBloque)
            put("cantidad", cantidad)
            put("id_usuario_carga", idUsuario)
            if (idAcceso == null) putNull("id_acceso") else put("id_acceso", idAcceso)
            if (momentoCorreccion != null) {
                put("id_usuario_correccion", idUsuario)
                put("fecha_correccion", momentoCorreccion)
            }
        }
        helper.writableDatabase.insert("conteo_ingreso", null, valores)
    }
}
