package com.example.rinconsur

import com.example.rinconsur.data.Rut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class RutTest {

    @Test
    fun losRutDelAnexoSonValidos() {
        assertNull(Rut.validar("19.876.345-4"))
        assertNull(Rut.validar("17.654.320-5"))
        assertNull(Rut.validar("13.987.442-0"))
    }

    @Test
    fun aceptaElRutEscritoDeCualquierManera() {
        assertNull("con puntos y guion", Rut.validar("19.876.345-4"))
        assertNull("todo junto", Rut.validar("198763454"))
        assertNull("con espacios de mas", Rut.validar("  19.876.345-4 "))
        assertNull("digito K en minuscula", Rut.validar("11111112-k"))
        assertNull("digito K en mayuscula", Rut.validar("11111112-K"))
    }

    @Test
    fun rechazaUnDigitoVerificadorQueNoCorresponde() {
        assertNotNull(Rut.validar("19.876.345-5"))
        assertNotNull(Rut.validar("17.654.320-4"))
        assertNotNull(Rut.validar("13.987.442-1"))
    }

    @Test
    fun rechazaLoQueNoEsUnRut() {
        assertNotNull("vacio", Rut.validar(""))
        assertNotNull("palabras", Rut.validar("no tengo rut"))
        assertNotNull("muy corto", Rut.validar("12.345-6"))
        assertNotNull("letras en el cuerpo", Rut.validar("19.876.34x-4"))
    }

    @Test
    fun elFormatoCanonicoEsElQueSeGuarda() {
        assertEquals("19.876.345-4", Rut.formatear("198763454"))
        assertEquals("19.876.345-4", Rut.formatear("19.876.345-4"))
        assertEquals("11.111.112-K", Rut.formatear("11111112k"))
    }
}
