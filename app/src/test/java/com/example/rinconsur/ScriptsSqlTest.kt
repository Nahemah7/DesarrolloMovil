package com.example.rinconsur

import com.example.rinconsur.data.dividirSentencias
import org.junit.Assert.assertEquals
import org.junit.Test

class ScriptsSqlTest {

    @Test
    fun divisor_separaPorPuntoYComa() {
        val script = "CREATE TABLE a (x INT);\nINSERT INTO a VALUES (1);"

        val sentencias = dividirSentencias(script)

        assertEquals(2, sentencias.size)
        assertEquals("CREATE TABLE a (x INT)", sentencias[0])
        assertEquals("INSERT INTO a VALUES (1)", sentencias[1])
    }

    @Test
    fun divisor_noRompeElPuntoYComaDentroDeUnTexto() {
        val script = "INSERT INTO rol VALUES (1, 'Consulta indicadores; registra la meta');"

        val sentencias = dividirSentencias(script)

        assertEquals(1, sentencias.size)
        assertEquals("INSERT INTO rol VALUES (1, 'Consulta indicadores; registra la meta')", sentencias[0])
    }

    @Test
    fun divisor_ignoraLosComentariosConPuntoYComa() {
        val script = "-- uno; dos; tres\nSELECT 1;"

        val sentencias = dividirSentencias(script)

        assertEquals(1, sentencias.size)
        assertEquals("SELECT 1", sentencias[0])
    }

    @Test
    fun divisor_ignoraElFinalVacio() {
        val sentencias = dividirSentencias("SELECT 1;;;\n")

        assertEquals(1, sentencias.size)
    }
}
