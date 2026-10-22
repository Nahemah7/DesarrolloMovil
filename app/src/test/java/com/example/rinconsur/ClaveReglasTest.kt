package com.example.rinconsur

import com.example.rinconsur.data.esClaveValida
import com.example.rinconsur.data.mensajeDeClave
import com.example.rinconsur.data.problemasDeClave
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Reglas de la clave: 8 caracteres, con mayuscula, minuscula y numero.
 * Se prueban en JVM, sin emulador ni base.
 */
class ClaveReglasTest {

    @Test fun `una clave que cumple todo es valida`() {
        assertTrue(esClaveValida("RinconSur1"))
        assertTrue(problemasDeClave("RinconSur1").isEmpty())
    }

    @Test fun `ocho caracteres exactos alcanzan`() {
        assertTrue(esClaveValida("RinconS1"))
    }

    @Test fun `siete caracteres no alcanzan y lo dice`() {
        assertFalse(esClaveValida("RincoS1"))
        assertEquals(listOf("al menos 8 caracteres"), problemasDeClave("RincoS1"))
    }

    @Test fun `sin mayuscula falla solo por eso`() {
        assertEquals(listOf("una mayúscula"), problemasDeClave("rinconsur1"))
    }

    @Test fun `sin minuscula falla solo por eso`() {
        assertEquals(listOf("una minúscula"), problemasDeClave("RINCONSUR1"))
    }

    @Test fun `sin numero falla solo por eso`() {
        assertEquals(listOf("un número"), problemasDeClave("RinconSur"))
    }

    @Test fun `vacia lista los cuatro requisitos`() {
        assertEquals(4, problemasDeClave("").size)
    }

    @Test fun `solo digitos no sirve como clave`() {
        val faltas = problemasDeClave("12345678")
        assertTrue(faltas.contains("una mayúscula") && faltas.contains("una minúscula"))
        assertFalse(esClaveValida("12345678"))
    }

    @Test fun `el mensaje nombra lo que falta y es nulo si esta bien`() {
        assertNotNull(mensajeDeClave("rinconsur"))
        assertTrue(mensajeDeClave("rinconsur")!!.contains("mayúscula"))
        assertNull(mensajeDeClave("RinconSur1"))
    }
}
