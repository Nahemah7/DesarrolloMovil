package com.example.rinconsur.data

import android.database.Cursor
import com.example.rinconsur.model.RegistroPendiente

// La cola de salida: lo que está guardado en el teléfono y todavía no llegó al servidor.
// Es una tabla de la misma base local, así que un registro y su marca de "pendiente" se
// escriben en la misma transacción: no existe el caso de un registro que se pierda en el medio.
internal class ColaDao(private val helper: FlujoDbHelper) {

    fun pendientes(limite: Int = 200): List<RegistroPendiente> {
        val lista = mutableListOf<RegistroPendiente>()
        helper.readableDatabase.rawQuery(
            "SELECT r.id_registro, r.id_sesion, s.id_remoto, r.fecha_hora, r.id_clasificacion, r.id_rango, " +
                "r.id_tipo_compania, r.compras_otra_tienda, r.id_acceso_destino " +
                "FROM registro_observacion r JOIN sesion_observacion s ON s.id_sesion = r.id_sesion " +
                "WHERE r.enviado = 0 AND r.anulado = 0 ORDER BY r.id_registro LIMIT ?",
            arrayOf(limite.toString())
        ).use { c ->
            while (c.moveToNext()) lista.add(leer(c))
        }
        return lista
    }

    fun contarPendientes(): Int =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM registro_observacion WHERE enviado = 0 AND anulado = 0", arrayOf()
        ).use { c -> if (c.moveToFirst()) c.getInt(0) else 0 }

    fun marcarEnviado(idLocal: Long, idRemoto: Long) {
        helper.writableDatabase.execSQL(
            "UPDATE registro_observacion SET enviado = 1, id_remoto = ? WHERE id_registro = ?",
            arrayOf(idRemoto, idLocal)
        )
    }

    private fun leer(c: Cursor) = RegistroPendiente(
        idLocal = c.getLong(0),
        idSesion = c.getLong(1),
        idSesionRemota = if (c.isNull(2)) null else c.getLong(2),
        fechaHora = c.getString(3).replace(" ", "T"),
        idClasificacion = c.getLong(4),
        idRango = c.getLong(5),
        idTipoCompania = c.getLong(6),
        comprasOtraTienda = c.getInt(7) == 1,
        idAccesoDestino = if (c.isNull(8)) null else c.getLong(8)
    )
}
