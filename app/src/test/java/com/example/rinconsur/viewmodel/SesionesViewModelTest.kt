package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.model.EstadoSesion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// La supervisora revisa sesiones ya cerradas. Si el filtro deja pasar una sesión que sigue en
// curso, el botón de validar aparece donde no corresponde y se puede cerrar el turno de alguien
// que está midiendo en ese momento.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SesionesViewModelTest {

    private lateinit var application: Application
    private lateinit var vm: SesionesViewModel

    @Before
    fun preparar() {
        application = ApplicationProvider.getApplicationContext()
        vm = SesionesViewModel(application)
    }

    @Test
    fun filtrar_porValidar_dejaSoloLasCerradasYElFiltroPuesto() {
        vm.filtrar("Por validar")

        val estado = vm.uiState.value

        assertEquals("Por validar", estado.filtro)
        // Las que esperan revisión son las cerradas: no las que están en curso ni las ya validadas.
        assertTrue(
            "Una sesión por validar no puede seguir en curso",
            estado.porValidar.none { it.estado == EstadoSesion.EN_CURSO }
        )
        assertTrue(
            "Todas las que esperan revisión están cerradas",
            estado.porValidar.all { it.estado == EstadoSesion.FINALIZADA }
        )
        assertTrue(
            "Y el estado del que espera revisión se declara como tal",
            EstadoSesion.FINALIZADA.esperaRevision
        )
    }

    @Test
    fun alternarSoloMias_marcaYDesmarcaElFiltroDelRevisor() {
        vm.alternarSoloMias()
        assertTrue("El primer toque deja el filtro puesto", vm.uiState.value.soloMias)

        vm.alternarSoloMias()
        assertTrue("El segundo toque lo saca", !vm.uiState.value.soloMias)
    }

    @Test
    fun limpiarFiltros_devuelveLaListaCompletaSinAccesoNiObservador() {
        vm.filtrarAcceso(4)
        vm.filtrarObservador(5)
        vm.alternarSoloMias()

        vm.limpiarFiltros()

        val estado = vm.uiState.value
        assertNull(estado.idAcceso)
        assertNull(estado.idObservador)
        assertTrue(!estado.soloMias)
    }

    @Test
    fun filtrarAcceso_dosVecesElMismoAcceso_quitaElFiltro() {
        vm.filtrarAcceso(4)
        assertEquals(4L, vm.uiState.value.idAcceso)

        // El mismo botón que filtra vuelve a mostrar todo: pulsarlo dos veces no deja la lista vacía.
        vm.filtrarAcceso(4)
        assertNull(vm.uiState.value.idAcceso)
    }

    @Test
    fun elegirFecha_cambiaElDiaQueSeEstaMirando() {
        vm.elegirFecha("2026-09-15")

        val estado = vm.uiState.value

        assertEquals("2026-09-15", estado.fecha)
        assertNotNull("La fecha larga se arma para la cabecera", estado.fechaLarga)
        assertEquals("Elegir un día deja la vista en ese día", "Día", estado.filtro)
    }

    @Test
    fun moverDiaAtras_mantieneElFiltroDeDiaYNoSeVaDelCalendario() {
        vm.elegirFecha("2026-09-15")

        vm.diaAnterior()

        assertEquals("2026-09-14", vm.uiState.value.fecha)
        assertEquals("Día", vm.uiState.value.filtro)
    }
}
