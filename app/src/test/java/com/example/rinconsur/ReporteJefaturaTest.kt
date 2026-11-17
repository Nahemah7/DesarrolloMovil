package com.example.rinconsur

import com.example.rinconsur.model.ConversionMes
import com.example.rinconsur.model.HoraPunta
import com.example.rinconsur.model.MetaDelMes
import com.example.rinconsur.model.ReporteJefatura
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReporteJefaturaTest {

    @Test
    fun meta_sinObjetivoRegistrado_noTieneCumplimiento() {
        val meta = MetaDelMes(anio = 2026, mes = 11, objetivo = null, actual = 8.7)

        assertFalse(meta.registrada)
        assertNull(meta.cumplimiento)
        assertEquals("sin dato", meta.cumplimientoTexto)
    }

    @Test
    fun meta_conObjetivoYConversion_loMuestraComoAvance() {
        val meta = MetaDelMes(anio = 2026, mes = 11, objetivo = 10.0, actual = 8.7)

        assertEquals(87.0, meta.cumplimiento!!, 0.001)
        assertEquals("87,0 %", meta.cumplimientoTexto)
        assertEquals(0.87f, meta.avance, 0.001f)
    }

    @Test
    fun meta_sinConversionDelMes_noInventaAvance() {
        val meta = MetaDelMes(anio = 2026, mes = 11, objetivo = 10.0, actual = null)

        assertNull(meta.cumplimiento)
        assertEquals(0f, meta.avance, 0.001f)
    }

    @Test
    fun meta_cuandoSuperaElObjetivo_laBarraNoSeSale() {
        val meta = MetaDelMes(anio = 2026, mes = 11, objetivo = 5.0, actual = 12.0)

        assertEquals(240.0, meta.cumplimiento!!, 0.001)
        assertEquals(1f, meta.avance, 0.001f)
    }

    @Test
    fun meta_conObjetivoCero_noDivide() {
        val meta = MetaDelMes(anio = 2026, mes = 11, objetivo = 0.0, actual = 8.7)

        assertNull(meta.cumplimiento)
    }

    @Test
    fun conversionMes_escribeElPeriodoEnEspanol() {
        val mes = ConversionMes(mes = "2026-09", ingresos = 5735, boletas = 497, conversion = 8.7, dias = 1)

        assertEquals("septiembre 2026", mes.periodo)
        assertEquals("8,7 %", mes.conversionTexto)
    }

    @Test
    fun conversionMes_sinConversion_loDice() {
        val mes = ConversionMes(mes = "2026-12", ingresos = 0, boletas = 0, conversion = null, dias = 0)

        assertEquals("sin dato", mes.conversionTexto)
    }

    @Test
    fun reporte_sinHorasMedidas_noTieneHoraPunta() {
        val reporte = ReporteJefatura(
            meta = MetaDelMes(2026, 11, null, null),
            horas = listOf(
                HoraPunta("10:00", ingresos = 559, transito = 0, medidos = 0, bloques = 1),
                HoraPunta("13:00", ingresos = 780, transito = 0, medidos = 0, bloques = 1)
            )
        )

        assertNull(reporte.horaPunta)
    }

    @Test
    fun reporte_conHorasMedidas_eligeLaDeMayorTransito() {
        val reporte = ReporteJefatura(
            meta = MetaDelMes(2026, 11, null, null),
            horas = listOf(
                HoraPunta("10:00", ingresos = 559, transito = 120, medidos = 2, bloques = 2),
                HoraPunta("13:00", ingresos = 780, transito = 293, medidos = 3, bloques = 3)
            )
        )

        assertEquals("13:00", reporte.horaPunta?.hora)
        assertTrue(reporte.horaPunta!!.medido)
    }
}
