package com.example.rinconsur

import com.example.rinconsur.model.alturaRelativa
import com.example.rinconsur.model.porciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SeriesGraficoTest {

    @Test
    fun porciones_repartenElCirculoCompleto() {
        val trozos = porciones(listOf("A" to 30, "B" to 20, "C" to 10))

        val total = trozos.sumOf { it.barridoGrados.toDouble() }
        assertEquals(360.0, total, 0.01)
        assertEquals(180.0, trozos[0].barridoGrados.toDouble(), 0.01)
        assertEquals(60.0, trozos[2].barridoGrados.toDouble(), 0.01)
    }

    @Test
    fun porciones_empiezanArribaYNoSeSolapan() {
        val trozos = porciones(listOf("A" to 1, "B" to 3))

        assertEquals(-90.0, trozos[0].inicioGrados.toDouble(), 0.01)
        assertEquals((trozos[0].inicioGrados + trozos[0].barridoGrados).toDouble(), trozos[1].inicioGrados.toDouble(), 0.01)
    }

    @Test
    fun porciones_losPorcentajesSumanCien() {
        val trozos = porciones(listOf("A" to 7, "B" to 2, "C" to 1))

        assertEquals(100.0, trozos.sumOf { it.porcentaje }, 0.2)
        assertEquals(70.0, trozos[0].porcentaje, 0.01)
    }

    @Test
    fun porciones_sinDatos_devuelveVacio() {
        assertTrue(porciones(emptyList()).isEmpty())
        assertTrue(porciones(listOf("A" to 0)).isEmpty())
    }

    @Test
    fun porciones_conCategoriaEnCero_noLeDaBarrido() {
        val trozos = porciones(listOf("A" to 10, "B" to 0))

        assertEquals(360.0, trozos[0].barridoGrados.toDouble(), 0.01)
        assertEquals(0.0, trozos[1].barridoGrados.toDouble(), 0.01)
    }

    @Test
    fun alturaRelativa_vaDeCeroAUnoFrenteAlMaximo() {
        assertEquals(0.0, alturaRelativa(0, 100).toDouble(), 0.001)
        assertEquals(0.5, alturaRelativa(50, 100).toDouble(), 0.001)
        assertEquals(1.0, alturaRelativa(100, 100).toDouble(), 0.001)
    }

    @Test
    fun alturaRelativa_sinMaximo_noDivide() {
        assertEquals(0.0, alturaRelativa(10, 0).toDouble(), 0.001)
    }
}
