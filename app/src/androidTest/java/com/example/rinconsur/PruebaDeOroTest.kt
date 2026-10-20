package com.example.rinconsur

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rinconsur.data.FlujoDbHelper
import com.example.rinconsur.model.OrigenTransito
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Prueba de oro: los numeros del Anexo 9.
 *
 * La base se crea de cero antes de correr, asi el resultado no depende de lo que se haya hecho en
 * el emulador. Si alguno de estos numeros se mueve, la base dejo de reproducir el caso.
 */
@RunWith(AndroidJUnit4::class)
class PruebaDeOroTest {

    private lateinit var db: FlujoDbHelper
    private val fecha = "2026-09-15"

    @Before
    fun abrirBaseLimpia() {
        val contexto = ApplicationProvider.getApplicationContext<android.content.Context>()
        contexto.deleteDatabase("rinconsur.db")     // se precarga sola en onCreate
        db = FlujoDbHelper(contexto)
    }

    @After
    fun cerrar() = db.close()

    private fun idDe(codigo: String): Long =
        db.sesionesDeFecha(fecha).first { it.codigo == codigo }.id

    @Test
    fun proporcionDeTransitoDeLasSesionesDelAnexo() {
        val s1 = db.resumenDeSesion(idDe("S-0001"))
        assertEquals("registros de S-0001", 118, s1.total)
        assertEquals("proporcion de transito de S-0001", 54.0, s1.pctTransito!!, 0.05)

        val s2 = db.resumenDeSesion(idDe("S-0002"))
        assertEquals("proporcion de transito de S-0002", 37.6, s2.pctTransito!!, 0.05)
    }

    @Test
    fun conversionDelDiaDelAnexo() {
        val dia = db.indicadoresDeFecha(fecha)
        assertNotNull("el dia tiene que existir", dia)
        assertEquals("ingresos del 15-09", 5735, dia!!.ingresos)
        assertEquals("boletas del 15-09", 497, dia.transacciones)
        assertEquals("conversion del 15-09 (RN-12)", 8.7, dia.conversionBruta!!, 0.05)
    }

    @Test
    fun transitoEstimadoMedidoDelAnexo() {
        // A4, 15-09, bloque de 13:00: 210 ingresos x 54,0 % = 113, y es medido porque hay sesion (RN-14).
        // Se lee la vista tal como la lee la app: el calculo es de la base, no de Kotlin.
        db.readableDatabase.rawQuery(
            "SELECT transito_estimado, origen FROM v_transito_bloque " +
                "WHERE fecha = ? AND acceso = 'A4' AND hora_inicio LIKE '13%'",
            arrayOf(fecha)
        ).use { c ->
            assertTrue("el bloque A4 de las 13:00 tiene que existir", c.moveToFirst())
            assertEquals("transito del A4 a las 13:00 (RN-14)", 113.0, c.getDouble(0), 0.5)
            assertEquals("el origen del dato", "medido", c.getString(1))
        }
    }
}
