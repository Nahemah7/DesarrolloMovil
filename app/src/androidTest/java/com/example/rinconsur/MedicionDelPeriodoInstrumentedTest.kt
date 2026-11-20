package com.example.rinconsur

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.rinconsur.data.FlujoRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

// El KPI principal de indicadores sale de los registros válidos, no de la carga de la tienda:
// por eso se puede mostrar el mismo día en que se mide, sin esperar los ingresos.
@RunWith(AndroidJUnit4::class)
class MedicionDelPeriodoInstrumentedTest {

    private lateinit var repositorio: FlujoRepository

    @Before
    fun preparar() {
        val contexto = InstrumentationRegistry.getInstrumentation().targetContext
        contexto.deleteDatabase("rinconsur.db")
        repositorio = FlujoRepository(contexto)
    }

    @Test
    fun el_dia_quince_de_septiembre_tiene_222_personas_medidas() {
        val medicion = repositorio.medicionDeFecha("2026-09-15")
        assertEquals(222, medicion.personas)
        assertEquals(2, medicion.bloques)
        assertEquals(1, medicion.dias)
    }

    @Test
    fun los_dias_habiles_suman_las_sesiones_medidas() {
        val medicion = repositorio.medicionDeTipo("Habil")
        assertEquals("15-09 (222) + 22-09 (36)", 258, medicion.personas)
        assertEquals(3, medicion.bloques)
    }

    @Test
    fun un_dia_sin_mediciones_devuelve_cero_y_no_una_fila_nula() {
        val medicion = repositorio.medicionDeFecha("2026-11-01")
        assertEquals(0, medicion.personas)
        assertEquals(0, medicion.bloques)
    }

    @Test
    fun al_borrar_los_datos_de_ejemplo_la_medicion_queda_en_cero() {
        // El borrado se lleva las sesiones y sus registros: la medición no sobrevive a eso.
        repositorio.borrarDatosDeEjemplo()
        assertEquals(0, repositorio.medicionDeFecha("2026-09-15").personas)
    }
}
