package com.example.rinconsur

import com.example.rinconsur.model.Genero
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// El cargo de cada perfil se escribe según la persona: jefa o jefe, supervisora o supervisor.
class CargosPorGeneroTest {

    @Test
    fun cadaPerfilTieneSusDosFormas() {
        assertEquals("Jefa de tienda", PerfilUsuario.JEFE_TIENDA.etiqueta(Genero.FEMENINO))
        assertEquals("Jefe de tienda", PerfilUsuario.JEFE_TIENDA.etiqueta(Genero.MASCULINO))
        assertEquals("Supervisora de piso", PerfilUsuario.SUPERVISOR.etiqueta(Genero.FEMENINO))
        assertEquals("Supervisor de piso", PerfilUsuario.SUPERVISOR.etiqueta(Genero.MASCULINO))
        assertEquals("Colaboradora observadora", PerfilUsuario.OBSERVADOR.etiqueta(Genero.FEMENINO))
        assertEquals("Colaborador observador", PerfilUsuario.OBSERVADOR.etiqueta(Genero.MASCULINO))
        assertEquals("Administradora", PerfilUsuario.ADMINISTRADOR.etiqueta(Genero.FEMENINO))
        assertEquals("Administrador", PerfilUsuario.ADMINISTRADOR.etiqueta(Genero.MASCULINO))
    }

    @Test
    fun laSubgerenciaMantieneLaFormaSinCambio() {
        assertEquals("Subgerenta comercial", PerfilUsuario.SUBGERENTE.etiqueta(Genero.FEMENINO))
        assertEquals("Subgerente comercial", PerfilUsuario.SUBGERENTE.etiqueta(Genero.MASCULINO))
    }

    @Test
    fun sinGeneroElegido_seUsaLaFormaMasculina() {
        assertEquals("Supervisor de piso", PerfilUsuario.SUPERVISOR.etiqueta)
    }

    @Test
    fun cadaPerfilTieneUnNombreDeGrupoPropio() {
        val grupos = PerfilUsuario.entries.map { it.grupo }
        assertEquals(grupos.size, grupos.toSet().size)
    }

    @Test
    fun elGeneroSeLeeDelCodigoGuardado() {
        assertEquals(Genero.MASCULINO, Genero.porCodigo("M"))
        assertEquals(Genero.FEMENINO, Genero.porCodigo("f"))
        assertEquals(Genero.FEMENINO, Genero.porCodigo(null))
    }

    @Test
    fun unaPersonaSinGeneroElegidoNaceFemeninaYSuCargoLoRespeta() {
        val persona = NuevaPersona(
            rut = "19.111.222-9",
            nombres = "Camila",
            apellidoPaterno = "Vergara",
            correo = "camila@rinconsur-ficticio.ejemplo",
            rol = PerfilUsuario.SUPERVISOR
        )

        assertEquals(Genero.FEMENINO, persona.genero)
        assertTrue(persona.puedeCrear)
        assertEquals("Supervisora de piso", persona.rol!!.etiqueta(persona.genero))
    }

    @Test
    fun unHombreQuedaConElCargoEnMasculino() {
        val persona = NuevaPersona(
            rut = "19.111.222-9",
            nombres = "Pedro",
            apellidoPaterno = "Leiva",
            correo = "pedro@rinconsur-ficticio.ejemplo",
            rol = PerfilUsuario.SUPERVISOR,
            genero = Genero.MASCULINO
        )

        assertTrue(persona.puedeCrear)
        assertEquals("Supervisor de piso", persona.rol!!.etiqueta(persona.genero))
        assertFalse(persona.errores.contains("Elige el perfil."))
    }
}
