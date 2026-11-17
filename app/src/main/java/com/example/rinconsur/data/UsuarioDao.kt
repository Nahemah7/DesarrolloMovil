package com.example.rinconsur.data

import android.content.ContentValues
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.PreguntaSeguridad
import com.example.rinconsur.model.PerfilUsuario

internal class UsuarioDao(private val helper: FlujoDbHelper) {

    fun buscarColaboradorPorRut(rut: String): Colaborador? =
        consultarColaboradores(FlujoDbHelper.SQL_USUARIO + "WHERE u.rut = ?", arrayOf(rut)).firstOrNull()

    fun buscarColaboradorPorId(id: Long): Colaborador? =
        consultarColaboradores(FlujoDbHelper.SQL_USUARIO + "WHERE u.id_usuario = ?", arrayOf(id.toString())).firstOrNull()

    fun cambiarNombre(idUsuario: Long, nombres: String, apellidoPaterno: String): Int {
        val valores = ContentValues().apply {
            put("nombres", nombres)
            put("apellido_paterno", apellidoPaterno)
        }
        return helper.writableDatabase.update("usuario", valores, "id_usuario = ?", arrayOf(idUsuario.toString()))
    }

    fun listarPreguntas(): List<PreguntaSeguridad> {
        val lista = mutableListOf<PreguntaSeguridad>()
        helper.readableDatabase.rawQuery(
            "SELECT id_pregunta, texto FROM pregunta_seguridad WHERE activa = 1 ORDER BY id_pregunta", null
        ).use { c -> while (c.moveToNext()) lista.add(PreguntaSeguridad(id = c.getLong(0), texto = c.getString(1))) }
        return lista
    }

    fun listarPreguntasDe(idUsuario: Long): List<PreguntaSeguridad> {
        val lista = mutableListOf<PreguntaSeguridad>()
        helper.readableDatabase.rawQuery(
            """SELECT p.id_pregunta, p.texto FROM respuesta_seguridad r
               JOIN pregunta_seguridad p ON p.id_pregunta = r.id_pregunta
               WHERE r.id_usuario = ? ORDER BY p.id_pregunta""",
            arrayOf(idUsuario.toString())
        ).use { c -> while (c.moveToNext()) lista.add(PreguntaSeguridad(id = c.getLong(0), texto = c.getString(1))) }
        return lista
    }

    fun tienePreguntas(idUsuario: Long): Boolean =
        helper.readableDatabase.rawQuery(
            "SELECT COUNT(*) FROM respuesta_seguridad WHERE id_usuario = ?", arrayOf(idUsuario.toString())
        ).use { c -> c.moveToFirst() && c.getInt(0) >= 1 }

    fun guardarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>) {
        helper.writableDatabase.beginTransaction()
        try {
            helper.writableDatabase.delete("respuesta_seguridad", "id_usuario = ?", arrayOf(idUsuario.toString()))
            respuestas.forEach { (idPregunta, respuesta) ->
                val valores = ContentValues().apply {
                    put("id_usuario", idUsuario)
                    put("id_pregunta", idPregunta)
                    put("respuesta_hash", Clave.hash(rut, normalizarRespuesta(respuesta)))
                }
                helper.writableDatabase.insert("respuesta_seguridad", null, valores)
            }
            helper.writableDatabase.setTransactionSuccessful()
        } finally {
            helper.writableDatabase.endTransaction()
        }
    }

    fun verificarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>): Boolean {
        if (respuestas.isEmpty()) return false
        var correctas = 0
        respuestas.forEach { (idPregunta, respuesta) ->
            helper.readableDatabase.rawQuery(
                "SELECT respuesta_hash FROM respuesta_seguridad WHERE id_usuario = ? AND id_pregunta = ?",
                arrayOf(idUsuario.toString(), idPregunta.toString())
            ).use { c ->
                if (c.moveToFirst() && c.getString(0) == Clave.hash(rut, normalizarRespuesta(respuesta))) correctas++
            }
        }
        return correctas == respuestas.size
    }

    fun guardarFoto(idUsuario: Long, foto: ByteArray?): Boolean {
        val valores = ContentValues().apply {
            if (foto == null || foto.isEmpty()) putNull("foto") else put("foto", foto)
        }
        return helper.writableDatabase.update(
            "usuario", valores, "id_usuario = ?", arrayOf(idUsuario.toString())
        ) == 1
    }

    fun fotoDe(idUsuario: Long): ByteArray? {
        helper.readableDatabase.query(
            "usuario", arrayOf("foto"), "id_usuario = ?", arrayOf(idUsuario.toString()),
            null, null, null
        ).use { c ->
            if (c.moveToFirst() && !c.isNull(0)) return c.getBlob(0)
        }
        return null
    }

    fun cambiarClave(idUsuario: Long, nuevoHash: String): Boolean =
        helper.writableDatabase.update(
            "usuario", ContentValues().apply { put("password_hash", nuevoHash) },
            "id_usuario = ?", arrayOf(idUsuario.toString())
        ) == 1

    private fun normalizarRespuesta(respuesta: String): String = respuesta.trim().lowercase()

    fun listarObservadores(): List<Colaborador> =
        consultarColaboradores(
            FlujoDbHelper.SQL_USUARIO + "WHERE u.id_rol = (SELECT id_rol FROM rol WHERE nombre LIKE '%observador%')",
            arrayOf()
        )

    fun listarColaboradores(): List<Colaborador> =
        consultarColaboradores(FlujoDbHelper.SQL_USUARIO + "ORDER BY u.id_rol, u.nombres", arrayOf())

    private fun consultarColaboradores(sql: String, args: Array<String>?): List<Colaborador> {
        val lista = mutableListOf<Colaborador>()
        helper.readableDatabase.rawQuery(sql, args).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Colaborador(
                        id = c.long("id_usuario"),
                        rut = c.texto("rut"),
                        nombre = c.texto("nombres"),
                        apellido = c.texto("apellido_paterno"),
                        cargo = c.texto("cargo"),
                        perfil = PerfilUsuario.porCodigo(c.texto("rol_codigo")) ?: PerfilUsuario.OBSERVADOR,
                        activo = c.entero("activo") == 1,
                        passwordHash = c.texto("password_hash")
                    )
                )
            }
        }
        return lista
    }
}
