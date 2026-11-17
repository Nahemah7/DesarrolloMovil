package com.example.rinconsur

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rinconsur.data.FlujoDbHelper
import com.example.rinconsur.data.FlujoRepository
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Carga del día de punta a punta sobre la base real del teléfono: se guarda, se relee y
// se comprueba que las métricas del día reflejan lo cargado.
@RunWith(AndroidJUnit4::class)
class CargaDelDiaInstrumentedTest {

    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repositorio: FlujoRepository
    private val fecha = "2026-11-18"
    private val idUsuario = 2L

    @Before
    fun preparar() {
        repositorio = FlujoRepository(contexto)
        limpiar()
    }

    @After
    fun terminar() {
        limpiar()
        repositorio.cerrar()
    }

    private fun limpiar() {
        val db = FlujoDbHelper(contexto).writableDatabase
        db.delete("conteo_ingreso", "fecha = ?", arrayOf(fecha))
        db.delete("transaccion_bloque", "fecha = ?", arrayOf(fecha))
    }

    private fun dia(vararg bloques: Pair<Long, Pair<String, String>>) =
        repositorio.cargaDelDia(fecha).copy(
            bloques = repositorio.cargaDelDia(fecha).bloques.map { bloque ->
                val datos = bloques.firstOrNull { it.first == bloque.idBloque }?.second
                if (datos == null) bloque else bloque.copy(ingresos = datos.first, boletas = datos.second)
            }
        )

    @Test
    fun cargaPorBloque_seGuardaYSeReleeIgual() {
        val carga = dia(1L to ("235" to "18"), 2L to ("355" to "30"))

        assertTrue(repositorio.guardarCargaDelDia(carga, idUsuario))

        val guardado = repositorio.cargaDelDia(fecha)
        assertEquals("235", guardado.bloques[0].ingresos)
        assertEquals("18", guardado.bloques[0].boletas)
        assertEquals("355", guardado.bloques[1].ingresos)
        assertEquals("30", guardado.bloques[1].boletas)
        assertEquals(590, guardado.totalIngresos)
        assertEquals(48, guardado.totalBoletas)
        assertEquals(8.1, guardado.conversion!!, 0.01)
        assertFalse(guardado.bloques[0].detallado)

        val db = FlujoDbHelper(contexto).readableDatabase
        db.rawQuery(
            "SELECT COUNT(*) FROM conteo_ingreso WHERE fecha = ? AND id_acceso IS NULL", arrayOf(fecha)
        ).use { c -> c.moveToFirst(); assertEquals(2, c.getInt(0)) }
        db.rawQuery(
            "SELECT COUNT(*) FROM transaccion_bloque WHERE fecha = ?", arrayOf(fecha)
        ).use { c -> c.moveToFirst(); assertEquals(2, c.getInt(0)) }
    }

    @Test
    fun cargaConRepartoPorAcceso_guardaUnaFilaPorAcceso() {
        val base = repositorio.cargaDelDia(fecha)
        val accesos = base.accesos
        assertEquals(4, accesos.size)

        val bloque = base.bloques[3].copy(ingresos = "100", boletas = "9", detallado = true)
            .conDetalle(accesos[0].id, "40")
            .conDetalle(accesos[1].id, "30")
            .conDetalle(accesos[2].id, "20")
            .conDetalle(accesos[3].id, "10")
        val carga = base.copy(bloques = base.bloques.map { if (it.idBloque == bloque.idBloque) bloque else it })

        assertTrue(carga.puedeGuardar)
        assertTrue(repositorio.guardarCargaDelDia(carga, idUsuario))

        val guardado = repositorio.cargaDelDia(fecha)
        val releido = guardado.bloques.first { it.idBloque == bloque.idBloque }
        assertTrue(releido.detallado)
        assertEquals(4, releido.detalle.size)
        assertEquals(100, releido.sumaDetalle)
        assertTrue(releido.detalleCuadra)

        val db = FlujoDbHelper(contexto).readableDatabase
        db.rawQuery(
            "SELECT COUNT(*) FROM conteo_ingreso WHERE fecha = ? AND id_acceso IS NOT NULL", arrayOf(fecha)
        ).use { c -> c.moveToFirst(); assertEquals(4, c.getInt(0)) }
    }

    @Test
    fun lasMetricasDelDiaReflejanLoCargado() {
        val carga = dia(1L to ("235" to "18"), 2L to ("355" to "30"))
        assertTrue(repositorio.guardarCargaDelDia(carga, idUsuario))

        val indicadores = repositorio.indicadoresDeFecha(fecha)
        assertNotNull(indicadores)
        assertEquals(590, indicadores!!.ingresos)
        assertEquals(48, indicadores.transacciones)
        assertEquals(8.1, indicadores.conversionBruta!!, 0.01)
        assertEquals("incompleto", indicadores.estadoDia)
    }

    @Test
    fun reguardarElMismoDia_noDuplicaFilasYQuedaComoCorreccion() {
        val primera = dia(1L to ("235" to "18"))
        assertTrue(repositorio.guardarCargaDelDia(primera, idUsuario))

        val segunda = dia(1L to ("240" to "20"))
        assertTrue(repositorio.guardarCargaDelDia(segunda, idUsuario))

        val guardado = repositorio.cargaDelDia(fecha)
        assertEquals("240", guardado.bloques[0].ingresos)
        assertEquals(240, guardado.totalIngresos)

        val db = FlujoDbHelper(contexto).readableDatabase
        db.rawQuery(
            "SELECT COUNT(*), SUM(CASE WHEN id_usuario_correccion IS NOT NULL THEN 1 ELSE 0 END) " +
                "FROM conteo_ingreso WHERE fecha = ?", arrayOf(fecha)
        ).use { c ->
            c.moveToFirst()
            assertEquals(1, c.getInt(0))
            assertEquals(1, c.getInt(1))
        }
        db.rawQuery(
            "SELECT COUNT(*) FROM transaccion_bloque WHERE fecha = ?", arrayOf(fecha)
        ).use { c -> c.moveToFirst(); assertEquals(1, c.getInt(0)) }
    }
}
