package com.example.rinconsur

import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.BloqueCarga
import com.example.rinconsur.model.CargaDia
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CargaDiaTest {

    private val accesos = listOf(
        Acceso(1, "A1", "Acceso Principal", "", ""),
        Acceso(2, "A2", "Acceso Lateral", "", "")
    )

    private fun dia(vararg bloques: BloqueCarga) = CargaDia(fecha = "2026-11-17", accesos = accesos, bloques = bloques.toList())

    private fun bloque(id: Long, ingresos: String = "", boletas: String = "") =
        BloqueCarga(idBloque = id, hora = "10:00", ingresos = ingresos, boletas = boletas)

    @Test
    fun conversion_seCalculaSobreLosTotalesDelDia() {
        val carga = dia(bloque(1, "100", "8"), bloque(2, "300", "20"))

        assertEquals(400, carga.totalIngresos)
        assertEquals(28, carga.totalBoletas)
        assertEquals(7.0, carga.conversion!!, 0.001)
        assertEquals("7,0 %", carga.conversionTexto)
    }

    @Test
    fun conversion_sinIngresos_noInventaElDato() {
        val carga = dia(bloque(1, "", "5"))

        assertNull(carga.conversion)
        assertEquals("sin dato", carga.conversionTexto)
    }

    @Test
    fun carga_sinDatos_noDejaGuardar() {
        val carga = dia(bloque(1), bloque(2))

        assertFalse(carga.puedeGuardar)
    }

    @Test
    fun carga_conUnBloque_dejaGuardar() {
        val carga = dia(bloque(1, "235", "18"))

        assertTrue(carga.puedeGuardar)
        assertEquals(1, carga.bloquesConDatos)
    }

    @Test
    fun detalle_queNoCuadraConElTotal_bloqueaElGuardado() {
        val carga = dia(bloque(1, "100", "8"))
            .conDetalle(1, 1, "60")
            .conDetalle(1, 2, "50")

        assertFalse(carga.puedeGuardar)
        assertTrue(carga.errores.first().contains("suma 110"))
    }

    @Test
    fun detalle_queCuadra_dejaGuardar() {
        val carga = dia(bloque(1, "100", "8"))
            .conDetalle(1, 1, "70")
            .conDetalle(1, 2, "30")

        assertTrue(carga.puedeGuardar)
        assertEquals(1, carga.bloquesConDetalle)
    }

    @Test
    fun quitarElDetalle_vuelveAlTotalDelBloque() {
        val carga = dia(bloque(1, "100", "8"))
            .conDetalle(1, 1, "100")
            .alternarDetalle(1)

        assertFalse(carga.bloque(1)!!.detallado)
        assertEquals(0, carga.bloquesConDetalle)
        assertTrue(carga.puedeGuardar)
    }

    @Test
    fun textoQueNoEsNumero_seReporta() {
        val carga = dia(bloque(1, "12a", "8"))

        assertFalse(carga.puedeGuardar)
        assertTrue(carga.errores.first().contains("ingresos"))
    }

    @Test
    fun bloqueConIngresosSinBoletas_seCuentaComoIncompleto() {
        val carga = dia(bloque(1, "235", "18"), bloque(2, "355", ""))

        assertEquals(1, carga.bloquesAPartar)
    }

    @Test
    fun ceroBoletas_esUnDatoYNoUnVacio() {
        val carga = dia(bloque(1, "100", "0"))

        assertTrue(carga.bloque(1)!!.conBoletas)
        assertEquals(0, carga.totalBoletas)
        assertTrue(carga.puedeGuardar)
    }
}
