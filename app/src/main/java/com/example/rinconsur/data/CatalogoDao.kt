package com.example.rinconsur.data

import android.content.ContentValues
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.BloqueHorario
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.RangoEtario

internal class CatalogoDao(private val helper: FlujoDbHelper) {

    fun listarAccesos(): List<Acceso> {
        val lista = mutableListOf<Acceso>()
        helper.readableDatabase.rawQuery("SELECT * FROM acceso ORDER BY id_acceso", null).use { c ->
            while (c.moveToNext()) {
                lista.add(
                    Acceso(
                        id = c.long("id_acceso"),
                        codigo = c.texto("codigo"),
                        nombre = c.texto("nombre"),
                        ubicacion = c.texto("ubicacion"),
                        observacion = c.texto("observacion")
                    )
                )
            }
        }
        return lista
    }

    fun listarBloques(): List<BloqueHorario> {
        val lista = mutableListOf<BloqueHorario>()
        helper.readableDatabase.rawQuery("SELECT * FROM bloque_horario ORDER BY id_bloque", null).use { c ->
            while (c.moveToNext()) {
                lista.add(BloqueHorario(c.long("id_bloque"), c.texto("hora_inicio"), c.texto("hora_fin")))
            }
        }
        return lista
    }

    fun listarRangosEtarios(): List<Pair<RangoEtario, String>> =
        RangoEtario.entries.map { it to it.etiqueta }

    fun listarCompanias(): List<Compania> = Compania.entries

    fun fechaHoy(): String = FechaTienda.hoy()

    fun fechaDeTrabajo(): String =
        helper.readableDatabase.rawQuery("SELECT MAX(fecha) AS fecha FROM conteo_ingreso", null).use { c ->
            if (c.moveToNext()) c.texto("fecha") else ""
        }
}
