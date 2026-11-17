package com.example.rinconsur

import com.example.rinconsur.data.CodigoTemporal
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.PreguntaSeguridad
import com.example.rinconsur.model.PrimerAccesoUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PrimerAccesoTest {

    private val catalogo = listOf(
        PreguntaSeguridad(1, "¿Cuál es el nombre de tu primera mascota?"),
        PreguntaSeguridad(2, "¿En qué ciudad nació tu madre?")
    )

    private fun estado() = PrimerAccesoUiState(catalogo = catalogo)

    @Test
    fun codigoTemporal_tieneOchoCaracteresYUnGuion() {
        val codigo = CodigoTemporal.generar()

        assertEquals(9, codigo.length)
        assertEquals('-', codigo[4])
        assertTrue(CodigoTemporal.valido(codigo))
    }

    @Test
    fun codigoTemporal_noUsaCaracteresQueSeConfunden() {
        repeat(40) {
            val codigo = CodigoTemporal.generar()
            assertFalse(codigo.any { it in "01OI5S" })
        }
    }

    @Test
    fun codigoTemporal_rechazaUnCodigoMalEscrito() {
        assertFalse(CodigoTemporal.valido("ABCD1234"))
        assertFalse(CodigoTemporal.valido("ABCD-1234"))
        assertFalse(CodigoTemporal.valido("ABCD-123"))
    }

    @Test
    fun primerAcceso_conClaveDebil_avisaQueFalta() {
        val revisado = estado().copy(clave = "temporal", repetir = "temporal").validar()

        assertFalse(revisado.puedeGuardar)
        assertTrue(revisado.errorClave!!.contains("mayúscula"))
    }

    @Test
    fun primerAcceso_conClavesDistintas_avisaQueNoCoinciden() {
        val revisado = estado().copy(clave = "Rincon2026", repetir = "Rincon2025").validar()

        assertFalse(revisado.puedeGuardar)
        assertEquals("Las dos contraseñas no coinciden.", revisado.errorRepetir)
    }

    @Test
    fun primerAcceso_sinPregunta_noDejaGuardar() {
        val revisado = estado().copy(clave = "Rincon2026", repetir = "Rincon2026", respuesta = "Firulais").validar()

        assertFalse(revisado.puedeGuardar)
        assertEquals("Elige tu pregunta de seguridad.", revisado.errorPregunta)
    }

    @Test
    fun primerAcceso_conUnaPreguntaYRespuesta_dejaGuardar() {
        val estado = estado().copy(
            clave = "Rincon2026",
            repetir = "Rincon2026",
            idPregunta = 2,
            respuesta = "Valparaíso"
        )

        assertTrue(estado.validar().puedeGuardar)
        assertEquals("¿En qué ciudad nació tu madre?", estado.preguntaElegida?.texto)
    }

    @Test
    fun nuevaPersona_conDatosCompletos_sePuedeCrear() {
        val persona = NuevaPersona(
            rut = "19.111.222-9",
            nombres = "Camila",
            apellidoPaterno = "Vergara",
            correo = "camila.vergara@rinconsur-ficticio.ejemplo",
            rol = PerfilUsuario.OBSERVADOR
        )

        assertTrue(persona.errores.toString(), persona.puedeCrear)
    }

    @Test
    fun nuevaPersona_sinPerfil_noSePuedeCrear() {
        val persona = NuevaPersona(
            rut = "19.111.222-9",
            nombres = "Camila",
            apellidoPaterno = "Vergara",
            correo = "camila.vergara@rinconsur-ficticio.ejemplo"
        )

        assertFalse(persona.puedeCrear)
        assertTrue(persona.errores.contains("Elige el perfil."))
    }

    @Test
    fun nuevaPersona_conCorreoSinArroba_loRechaza() {
        val persona = NuevaPersona(
            rut = "19.111.222-9",
            nombres = "Camila",
            apellidoPaterno = "Vergara",
            correo = "camila.vergara",
            rol = PerfilUsuario.OBSERVADOR
        )

        assertFalse(persona.puedeCrear)
        assertTrue(persona.errores.contains("Revisa el correo."))
    }
}
