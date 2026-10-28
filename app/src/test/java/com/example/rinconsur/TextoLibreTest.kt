package com.example.rinconsur

import com.example.rinconsur.data.TEXTO_MAXIMO
import com.example.rinconsur.data.TEXTO_MINIMO
import com.example.rinconsur.data.mensajeDeTextoLibre
import com.example.rinconsur.model.MotivoRechazo
import com.example.rinconsur.model.textoDeRechazo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TextoLibreTest {

    private val vacio = "Escribe algo"

    @Test fun `un texto vacio pide lo que se le indico`() {
        assertEquals(vacio, mensajeDeTextoLibre("", vacio))
    }

    @Test fun `solo espacios cuenta como vacio`() {
        assertEquals(vacio, mensajeDeTextoLibre("      ", vacio))
    }

    @Test fun `nueve caracteres no alcanzan`() {
        assertNotNull(mensajeDeTextoLibre("a".repeat(TEXTO_MINIMO - 1), vacio))
    }

    @Test fun `diez caracteres exactos alcanzan`() {
        assertNull(mensajeDeTextoLibre("a".repeat(TEXTO_MINIMO), vacio))
    }

    @Test fun `doscientos caracteres exactos alcanzan`() {
        assertNull(mensajeDeTextoLibre("a".repeat(TEXTO_MAXIMO), vacio))
    }

    @Test fun `doscientos uno pasan el maximo y lo dice`() {
        val mensaje = mensajeDeTextoLibre("a".repeat(TEXTO_MAXIMO + 1), vacio)
        assertTrue(mensaje!!.contains(TEXTO_MAXIMO.toString()))
    }

    @Test fun `los espacios de los lados no cuentan`() {
        assertNotNull(mensajeDeTextoLibre("   corto   ", vacio))
        assertNull(mensajeDeTextoLibre("  " + "a".repeat(TEXTO_MINIMO) + "  ", vacio))
    }


    @Test fun `sin otro motivo se guardan solo las etiquetas`() {
        val texto = textoDeRechazo(
            setOf(MotivoRechazo.INTERRUMPIDA, MotivoRechazo.FUERA_DE_HORARIO),
            detalle = ""
        )
        assertEquals("Sesión interrumpida; Fuera del horario programado", texto)
    }

    @Test fun `con otro motivo se agrega el detalle sin espacios sobrantes`() {
        val texto = textoDeRechazo(setOf(MotivoRechazo.OTRO), detalle = "  se cayo la senal  ")
        assertEquals("Otro motivo: se cayo la senal", texto)
    }

    @Test fun `todas las etiquetas marcadas con un detalle de 10 caracteres caben en 200`() {
        val texto = textoDeRechazo(MotivoRechazo.entries.toSet(), detalle = "a".repeat(TEXTO_MINIMO))
        assertTrue(texto.length <= TEXTO_MAXIMO)
    }

    @Test fun `un detalle de 200 caracteres junto a otras etiquetas pasa el maximo`() {
        val texto = textoDeRechazo(
            setOf(MotivoRechazo.INTERRUMPIDA, MotivoRechazo.OTRO),
            detalle = "a".repeat(TEXTO_MAXIMO)
        )
        assertTrue(texto.length > TEXTO_MAXIMO)
    }
}