package com.example.rinconsur

import com.example.rinconsur.model.ProgramarUiState
import com.example.rinconsur.model.SesionResumen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas al programar una sesion de conteo: el horario dura entre 30 y 60 minutos y no se puede
 * pisar con otra sesion del mismo acceso ni de la misma persona.
 */
class ProgramarReglasTest {

    private fun sesion(codigo: String, acceso: Long, inicio: String, fin: String, observador: Long) =
        SesionResumen(
            id = 1, codigo = codigo, idAcceso = acceso, fecha = "2026-09-15",
            horaInicioProg = inicio, horaFinProg = fin, idObservador = observador
        )

    private fun estado(
        inicio: String, fin: String, acceso: Long, observador: Long, sesiones: List<SesionResumen>
    ) = ProgramarUiState(
        fecha = "2026-09-15", idAcceso = acceso, horaInicio = inicio, horaFin = fin,
        idObservador = observador, sesionesDeLaFecha = sesiones
    )

    @Test
    fun `una sesion de 45 minutos cumple el rango`() {
        val e = estado("13:00", "13:45", acceso = 1, observador = 7, sesiones = emptyList())
        assertEquals(45, e.duracionMinutos)
        assertTrue(e.duracionValida)
        assertTrue(e.sePuedeProgramar)
    }

    @Test
    fun `con menos de 30 minutos no se puede programar`() {
        val e = estado("13:00", "13:20", acceso = 1, observador = 7, sesiones = emptyList())
        assertFalse(e.duracionValida)
        assertFalse(e.sePuedeProgramar)
    }

    @Test
    fun `con mas de 60 minutos no se puede programar`() {
        val e = estado("13:00", "14:10", acceso = 1, observador = 7, sesiones = emptyList())
        assertFalse(e.duracionValida)
        assertFalse(e.sePuedeProgramar)
    }

    @Test
    fun `el mismo acceso ocupado en ese horario se cruza`() {
        val ocupada = sesion("S-0002", acceso = 2, inicio = "13:00", fin = "13:40", observador = 5)
        val e = estado("13:00", "13:45", acceso = 2, observador = 7, sesiones = listOf(ocupada))
        assertEquals("S-0002", e.choque?.codigo)
        assertFalse(e.sePuedeProgramar)
    }

    @Test
    fun `el observador con otra sesion en ese horario se cruza`() {
        val ocupada = sesion("S-0001", acceso = 4, inicio = "13:05", fin = "13:50", observador = 7)
        val e = estado("13:00", "13:45", acceso = 1, observador = 7, sesiones = listOf(ocupada))
        assertEquals("S-0001", e.choque?.codigo)
        assertFalse(e.sePuedeProgramar)
    }

    @Test
    fun `una sesion que termina justo cuando empieza la otra no se cruza`() {
        val anterior = sesion("S-0002", acceso = 2, inicio = "12:15", fin = "13:00", observador = 5)
        val e = estado("13:00", "13:45", acceso = 2, observador = 7, sesiones = listOf(anterior))
        assertNull(e.choque)
        assertTrue(e.sePuedeProgramar)
    }
}
