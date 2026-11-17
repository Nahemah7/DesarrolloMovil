package com.example.rinconsur.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// La base local: el esquema, las vistas y la semilla viven en app/src/main/assets/sql y se ejecutan
// al crear la base. Este archivo solo sabe de versiones, del orden de borrado y de leer los scripts.
class FlujoDbHelper(private val contexto: Context) :
    SQLiteOpenHelper(contexto, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "rinconsur.db"
        private const val VERSION_BD = 8

        private const val CARPETA = "sql"
        private const val SCRIPT_ESQUEMA = "01_esquema.sql"
        private const val SCRIPT_VISTAS = "02_vistas.sql"
        private const val SCRIPT_DATOS = "03_datos.sql"
        private const val SCRIPT_DEMO = "04_demo_sabado.sql"

        const val ESTADO_PROGRAMADA = 1L
        const val ESTADO_EN_CURSO = 2L
        const val ESTADO_FINALIZADA = 3L
        const val ESTADO_VALIDADA = 4L
        const val ESTADO_RECHAZADA = 5L

        const val MINIMO_REGISTROS = 30

internal const val SQL_USUARIO =
            "SELECT u.*, r.codigo AS rol_codigo FROM usuario u JOIN rol r ON r.id_rol = u.id_rol "

internal const val SQL_SESIONES =
            "SELECT s.*, u.nombres || ' ' || u.apellido_paterno AS nombre_observador, " +
                "e.nombre AS estado_nombre, COALESCE(v.registros_validos, 0) AS registros_validos, " +
                "COALESCE(v.transito, 0) AS transito, COALESCE(v.visita, 0) AS visita, " +
                "COALESCE(v.no_determinado, 0) AS no_determinado, v.pct_transito " +
                "FROM sesion_observacion s " +
                "JOIN usuario u ON u.id_usuario = s.id_observador " +
                "JOIN estado_sesion e ON e.id_estado = s.id_estado " +
                "LEFT JOIN v_sesion_proporcion v ON v.id_sesion = s.id_sesion "

        private val VISTAS = listOf(
            "v_conversion_dia", "v_transito_bloque", "v_pendiente_medir",
            "v_perfil_transito", "v_tipo_dia", "v_sesion_proporcion"
        )

        // Orden de borrado: primero las tablas que apuntan a otras y al final las que son
        // referenciadas. Al revés, con las claves foráneas activas, el DROP TABLE falla.
        private val TABLAS = listOf(
            "registro_observacion", "respuesta_seguridad", "sesion_observacion", "evento_dia",
            "conteo_ingreso", "transaccion_bloque", "meta_conversion", "usuario", "rol",
            "pregunta_seguridad", "acceso", "bloque_horario", "estado_sesion", "clasificacion",
            "rango_etario", "tipo_compania", "tipo_evento"
        )
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        ejecutar(db, SCRIPT_ESQUEMA)
        ejecutar(db, SCRIPT_VISTAS)
        ejecutar(db, SCRIPT_DATOS)
        ejecutar(db, SCRIPT_DEMO)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Base limpia: no hay datos de cliente real, así que en vez de encadenar migraciones se
        // borran las vistas y las tablas y se reconstruye todo desde los scripts.
        if (oldVersion < VERSION_BD) {
            VISTAS.forEach { db.execSQL("DROP VIEW IF EXISTS " + it) }
            TABLAS.forEach { db.execSQL("DROP TABLE IF EXISTS " + it) }
            onCreate(db)
        }
    }

    private fun ejecutar(db: SQLiteDatabase, archivo: String) {
        val script = contexto.assets.open(CARPETA + "/" + archivo).bufferedReader().use { it.readText() }
        dividirSentencias(script).forEach { db.execSQL(it) }
    }
}
