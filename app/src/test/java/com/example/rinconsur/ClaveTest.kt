package com.example.rinconsur

import com.example.rinconsur.data.Clave
import com.example.rinconsur.data.Rut
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** El hash de la clave: SHA-256 de "RUT|clave". La clave nunca se guarda en texto plano. */
class ClaveTest {

    @Test
    fun elHashEsEstableYHexadecimal() {
        val hash = Clave.hash("19.876.345-4", "rincon2026")
        assertEquals("largo de SHA-256 en hexadecimal", 64, hash.length)
        assertTrue("solo digitos hexadecimales", hash.all { it in "0123456789abcdef" })
        assertEquals("el mismo par da el mismo hash", hash, Clave.hash("19.876.345-4", "rincon2026"))
    }

    @Test
    fun elRutDelHashVaEnSuFormatoCanonico() {
        // Esta distincion importa: si un lado limpia el RUT y el otro no, el login falla sin decir por que.
        assertNotEquals(
            Clave.hash("19.876.345-4", "rincon2026"),
            Clave.hash("198763454", "rincon2026")
        )
        assertEquals(
            Clave.hash("19.876.345-4", "rincon2026"),
            Clave.hash(Rut.formatear("198763454"), "rincon2026")
        )
    }

    @Test
    fun laClaveYElRutCambianElHash() {
        assertNotEquals(Clave.hash("19.876.345-4", "uno"), Clave.hash("19.876.345-4", "otro"))
        assertNotEquals(Clave.hash("19.876.345-4", "rincon2026"), Clave.hash("17.654.320-5", "rincon2026"))
    }
}
