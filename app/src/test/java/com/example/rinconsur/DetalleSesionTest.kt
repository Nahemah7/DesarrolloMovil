package com.example.rinconsur

import com.example.rinconsur.model.DetalleSesion
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.model.SesionResumen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DetalleSesionTest {

    private fun sesion() = SesionResumen(
        id = 5,
        codigo = "S-0005",
        idAcceso = 3,
        fecha = "2026-09-22",
        horaInicioProg = "11:00:00",
        horaFinProg = "11:45:00",
        nombreObservador = "Tamara Rojas"
    )

    @Test
    fun grupo_sinPorcentaje_muestraSinDatoYFraccionCero() {
        val grupo = GrupoConteo("Tránsito", 61, null)

        assertEquals("sin dato", grupo.porcentajeTexto)
        assertEquals(0f, grupo.fraccion, 0.0001f)
    }

    @Test
    fun grupo_conPorcentaje_loMuestraConUnDecimalYFraccion() {
        val grupo = GrupoConteo("Tránsito", 61, 54.0)

        assertEquals("54,0 %", grupo.porcentajeTexto)
        assertEquals(0.54f, grupo.fraccion, 0.0001f)
    }

    @Test
    fun detalle_sinRegistros_loInforma() {
        assertTrue(DetalleSesion(sesion = sesion(), registrosValidos = 0).sinRegistros)
    }

    @Test
    fun detalle_conMarcas_calculaDuracionReal() {
        val detalle = DetalleSesion(
            sesion = sesion(),
            registrosValidos = 30,
            primeraMarca = "2026-09-22 11:05:00",
            ultimaMarca = "2026-09-22 11:40:00"
        )

        assertEquals(35, detalle.duracionRealMinutos)
        assertEquals("11:05", detalle.horaPrimera)
        assertEquals("11:40", detalle.horaUltima)
    }

    @Test
    fun detalle_sinMarcas_noCalculaDuracion() {
        val detalle = DetalleSesion(sesion = sesion(), registrosValidos = 30)

        assertNull(detalle.duracionRealMinutos)
    }
}
