package com.example.rinconsur

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.rinconsur.data.FlujoRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// Borrar los datos de ejemplo deja la aplicación sin mediciones, pero con sus personas
// y sus catálogos: es lo que el administrador usa antes de empezar a trabajar de verdad.
@RunWith(AndroidJUnit4::class)
class EmpezarDeCeroInstrumentedTest {

    private lateinit var repositorio: FlujoRepository

    @Before
    fun preparar() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        // El nombre tiene que ser el real: si no, se borra una base que no existe y las
        // pruebas comparten el estado de la anterior (una ya borró los datos de ejemplo).
        contexto.deleteDatabase("rinconsur.db")
        repositorio = FlujoRepository(contexto)
    }

    @Test
    fun la_semilla_marca_los_datos_como_de_ejemplo() {
        assertTrue("la semilla tiene que marcar los datos como de ejemplo", repositorio.datosDeEjemplo())
        assertTrue("la semilla trae sesiones", repositorio.sesionesDeFecha("2026-09-15").isNotEmpty())
    }

    @Test
    fun borrar_deja_sin_mediciones_y_conserva_personas_y_catalogos() {
        val personasAntes = repositorio.colaboradores().size
        val accesosAntes = repositorio.accesos().size

        assertTrue(repositorio.borrarDatosDeEjemplo())

        assertFalse("después de borrar ya no son datos de ejemplo", repositorio.datosDeEjemplo())
        assertTrue("no queda ninguna sesión", repositorio.sesionesDeFecha("2026-09-15").isEmpty())
        assertTrue("no quedan ingresos del día", repositorio.transitoPorAcceso("2026-09-15").isEmpty())
        assertEquals("las personas siguen ahí", personasAntes, repositorio.colaboradores().size)
        assertEquals("los accesos siguen ahí", accesosAntes, repositorio.accesos().size)
        assertTrue(
            "la meta del mes se va con los datos",
            repositorio.reporteJefatura().meta.objetivo == null
        )
        assertTrue(
            "el reporte deja de tener días medidos",
            repositorio.reporteJefatura().deEjemplo.not()
        )
    }

    @Test
    fun los_indicadores_quedan_vacios_pero_la_aplicacion_responde() {
        repositorio.borrarDatosDeEjemplo()
        assertEquals(null, repositorio.indicadoresDeFecha("2026-09-15"))
        assertTrue("siguen los cuatro accesos", repositorio.accesos().size >= 4)
        assertTrue("siguen los bloques horarios", repositorio.bloques().isNotEmpty())
        assertTrue("siguen los rangos etarios", repositorio.rangosEtarios().isNotEmpty())
        assertTrue("siguen los tipos de compañía", repositorio.companias().isNotEmpty())
    }
}
