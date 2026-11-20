package com.example.rinconsur.data

// Lo que no es medición: la marca de datos de ejemplo y el borrado de esos datos.
// Las personas, los catálogos y las preguntas de seguridad no se tocan.
internal class MantenimientoDao(private val helper: FlujoDbHelper) {

    fun datosDeEjemplo(): Boolean =
        helper.readableDatabase.rawQuery(
            "SELECT valor FROM configuracion WHERE clave = 'datos_ejemplo'", arrayOf()
        ).use { c -> c.moveToFirst() && c.getString(0) == "1" }

    fun borrarDatosDeEjemplo(): Boolean {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            // Primero los hijos, después los padres: las claves foráneas están activas.
            listOf(
                "registro_observacion",
                "evento_dia",
                "sesion_observacion",
                "conteo_ingreso",
                "transaccion_bloque",
                "meta_conversion"
            ).forEach { tabla -> db.delete(tabla, null, null) }
            db.execSQL(
                "INSERT OR REPLACE INTO configuracion (clave, valor, actualizado_en) " +
                    "VALUES ('datos_ejemplo', '0', datetime('now'))"
            )
            db.setTransactionSuccessful()
            return true
        } catch (e: Exception) {
            return false
        } finally {
            db.endTransaction()
        }
    }
}
