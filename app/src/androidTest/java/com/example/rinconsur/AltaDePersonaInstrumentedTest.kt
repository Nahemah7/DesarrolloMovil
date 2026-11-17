package com.example.rinconsur

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rinconsur.data.CodigoTemporal
import com.example.rinconsur.data.FlujoDbHelper
import com.example.rinconsur.data.FlujoRepository
import com.example.rinconsur.model.Genero
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Alta y edición de una persona sobre la base real: el cargo tiene que salir del perfil y el género.
@RunWith(AndroidJUnit4::class)
class AltaDePersonaInstrumentedTest {

    private val contexto: Context = ApplicationProvider.getApplicationContext()
    private lateinit var repositorio: FlujoRepository
    private val rut = "18.111.222-0"

    @Before
    fun preparar() {
        repositorio = FlujoRepository(contexto)
        borrar()
    }

    @After
    fun terminar() {
        borrar()
        repositorio.cerrar()
    }

    private fun borrar() {
        FlujoDbHelper(contexto).writableDatabase
            .delete("usuario", "rut = ?", arrayOf(rut))
    }

    private fun datos(genero: Genero) = NuevaPersona(
        rut = rut,
        nombres = "Pedro",
        apellidoPaterno = "Leiva",
        correo = "pedro.leiva@rinconsur-ficticio.ejemplo",
        telefono = "+56 9 1111 2222",
        rol = PerfilUsuario.SUPERVISOR,
        genero = genero
    )

    @Test
    fun unHombreSupervisorQuedaComoSupervisorDePiso() {
        val id = repositorio.crearColaborador(datos(Genero.MASCULINO), CodigoTemporal.generar())
        assertNotNull(id)

        val creado = repositorio.colaboradorPorId(id!!)!!
        assertEquals("Pedro", creado.nombre)
        assertEquals(Genero.MASCULINO, creado.genero)
        assertEquals("Supervisor de piso", creado.cargo)
        assertEquals(PerfilUsuario.SUPERVISOR, creado.perfil)
        assertEquals(true, creado.debeCambiarClave)
    }

    @Test
    fun cambiarElPerfilYElGeneroRecalculaElCargo() {
        val id = repositorio.crearColaborador(datos(Genero.MASCULINO), CodigoTemporal.generar())!!

        val cambiado = repositorio.actualizarColaborador(
            idUsuario = id,
            datos = datos(Genero.FEMENINO).copy(rut = rut),
            perfil = PerfilUsuario.OBSERVADOR,
            genero = Genero.FEMENINO,
            activo = true
        )

        assertEquals(true, cambiado)
        val despues = repositorio.colaboradorPorId(id)!!
        assertEquals(PerfilUsuario.OBSERVADOR, despues.perfil)
        assertEquals(Genero.FEMENINO, despues.genero)
        assertEquals("Colaboradora observadora", despues.cargo)
    }

    @Test
    fun desactivarLaCuentaNoBorraSusDatos() {
        val id = repositorio.crearColaborador(datos(Genero.MASCULINO), CodigoTemporal.generar())!!

        repositorio.actualizarColaborador(
            idUsuario = id,
            datos = datos(Genero.MASCULINO),
            perfil = PerfilUsuario.SUPERVISOR,
            genero = Genero.MASCULINO,
            activo = false
        )

        val despues = repositorio.colaboradorPorId(id)!!
        assertEquals(false, despues.activo)
        assertEquals("Supervisor de piso", despues.cargo)
    }
}
