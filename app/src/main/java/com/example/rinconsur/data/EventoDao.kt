package com.example.rinconsur.data

import android.content.ContentValues
import android.database.Cursor
import com.example.rinconsur.model.EventoDia
import com.example.rinconsur.model.TipoEvento

internal class EventoDao(private val helper: FlujoDbHelper) {

    fun registrarEvento(evento: EventoDia): Long =
        helper.writableDatabase.insert(
            "evento_dia", null,
            ContentValues().apply {
                put("codigo", evento.codigo)
                put("fecha_inicio", evento.fechaInicio)
                put("fecha_fin", evento.fechaFin)
                put("id_tipo_evento", evento.tipo.idTipo)
                put("id_acceso", evento.idAcceso)
                put("descripcion", evento.descripcion)
                put("id_usuario_registra", evento.idUsuarioRegistra)
                // La foto va como bytes (BLOB). Si el evento no lleva foto, queda NULL.
                put("foto", evento.foto)
            }
        )

    fun anularEvento(idEvento: Long, motivo: String, idColaborador: Long): Int =
        helper.writableDatabase.update(
            "evento_dia",
            ContentValues().apply {
                put("anulado", 1)
                put("motivo_anulacion", motivo)
                put("id_usuario_anula", idColaborador)
                put("fecha_anulacion", ahora())
            },
            "id_evento = ?", arrayOf(idEvento.toString())
        )

    fun contarTodosLosEventos(): Int =
        helper.readableDatabase.rawQuery("SELECT COUNT(*) AS n FROM evento_dia", null).use { c ->
            if (c.moveToNext()) c.entero("n") else 0
        }

    fun contarEventos(fecha: String): Int =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) AS n FROM evento_dia WHERE anulado = 0 AND ? BETWEEN fecha_inicio AND fecha_fin",
            arrayOf(fecha)
        ).use { c ->
            if (c.moveToNext()) c.entero("n") else 0
        }

    fun eventosRegistrados(): List<EventoDia> {
        val lista = mutableListOf<EventoDia>()
        helper.readableDatabase.rawQuery(
            "SELECT e.*, u.nombres AS nombre_registra FROM evento_dia e " +
                "LEFT JOIN usuario u ON u.id_usuario = e.id_usuario_registra " +
                "ORDER BY e.fecha_inicio DESC, e.id_evento DESC",
            null
        ).use { c ->
            while (c.moveToNext()) {
                lista.add(aEvento(c))
            }
        }
        return lista
    }

    private fun aEvento(c: Cursor): EventoDia =
        EventoDia(
            id = c.long("id_evento"),
            codigo = c.texto("codigo"),
            fechaInicio = c.texto("fecha_inicio"),
            fechaFin = c.texto("fecha_fin"),
            tipo = TipoEvento.entries.firstOrNull { it.idTipo == c.long("id_tipo_evento") } ?: TipoEvento.OTRO,
            idAcceso = c.longOpcional("id_acceso"),
            descripcion = c.texto("descripcion"),
            idUsuarioRegistra = c.long("id_usuario_registra"),
            foto = c.blobOpcional("foto"),
            nombreRegistra = c.texto("nombre_registra"),
            anulado = c.entero("anulado") == 1,
            motivoAnulacion = c.textoOpcional("motivo_anulacion")
        )
}
