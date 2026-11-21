package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.model.TipoPeriodo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Los indicadores se miran de tres maneras: el día de hoy, los hábiles y los fines de semana, y
// además se puede retroceder semana por semana. Un período mal elegido muestra las cifras de otro
// conjunto de días sin avisar, así que el ViewModel es justo lo que hay que probar.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IndicadoresViewModelTest {

    private lateinit var application: Application
    private lateinit var vm: IndicadoresViewModel

    @Before
    fun preparar() {
        application = ApplicationProvider.getApplicationContext()
        vm = IndicadoresViewModel(application)
    }

    @Test
    fun onPeriodo_enHoy_pideLasCifrasDelDiaYNoDeUnRango() {
        vm.onPeriodo(TipoPeriodo.HOY)

        val estado = vm.uiState.value
        assertEquals(TipoPeriodo.HOY, estado.periodo)
        // El día se pide por fecha: "hoy" no es un tipo de día agregado.
        assertEquals(estado.fecha, estado.indicadores?.desde ?: estado.fecha)
    }

    @Test
    fun onPeriodo_enHabiles_agregaPorTipoDeDiaYNoPorFechaSuelta() {
        vm.onPeriodo(TipoPeriodo.HABILES)

        val estado = vm.uiState.value
        assertEquals(TipoPeriodo.HABILES, estado.periodo)
        assertEquals("Habil", estado.tipoDia)
        assertTrue(
            "Los hábiles traen al menos un día con cifras en la base del anexo",
            (estado.indicadores?.dias ?: 0) >= 1
        )
    }

    @Test
    fun onSemanaAnterior_retrocedeUnaSemanaYMantieneElTipoDeDia() {
        vm.onPeriodo(TipoPeriodo.HABILES)
        val inicial = vm.uiState.value

        vm.onSemanaAnterior()

        val estado = vm.uiState.value
        assertEquals("La posición se mueve una semana atrás", inicial.posicionSemana + 1, estado.posicionSemana)
        assertEquals("No se cambia de tipo de día al navegar", TipoPeriodo.HABILES, estado.periodo)
        assertNotNull("La semana elegida trae su rango de fechas", estado.semanaElegida)
    }

    @Test
    fun onSemanaSiguiente_desdeLaSemanaMasVieja_vuelveHaciaAdelante() {
        vm.onPeriodo(TipoPeriodo.HABILES)
        vm.onSemanaAnterior()
        val atras = vm.uiState.value.posicionSemana

        vm.onSemanaSiguiente()

        assertEquals("Vuelve a la semana que estaba mirando", atras - 1, vm.uiState.value.posicionSemana)
    }

    @Test
    fun onSemanaAnterior_desdeHoy_pasaAMirarLaSemanaDeLosHabiles() {
        vm.onPeriodo(TipoPeriodo.HOY)

        // "Hoy" no es una semana: pedir la semana anterior cambia a los hábiles y retrocede una,
        // que es lo que espera quien pulsa la flecha mirando el día de hoy.
        vm.onSemanaAnterior()

        val estado = vm.uiState.value
        assertEquals(TipoPeriodo.HABILES, estado.periodo)
        assertEquals(1, estado.posicionSemana)
    }

    @Test
    fun recargar_conservaElPeriodoYLaSemanaElegida() {
        vm.onPeriodo(TipoPeriodo.HABILES)
        vm.onSemanaAnterior()
        val antes = vm.uiState.value

        vm.recargar()

        val despues = vm.uiState.value
        assertEquals("Recargar no devuelve la pantalla a hoy", antes.periodo, despues.periodo)
        assertEquals(antes.posicionSemana, despues.posicionSemana)
        assertEquals(antes.semanaElegida?.desde, despues.semanaElegida?.desde)
    }

    @Test
    fun estadoInicial_arrancaEnHoyYConLaFechaDeLaBase() {
        val estado = vm.uiState.value

        assertEquals(TipoPeriodo.HOY, estado.periodo)
        assertTrue("La fecha sale del repositorio, no del reloj del teléfono", estado.fecha.isNotBlank())
        assertNull("En el día de hoy no hay una semana elegida", estado.semanaElegida)
    }
}
