package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.model.TipoPeriodo
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Los indicadores se miran por día, por hábiles o por fin de semana, y se puede retroceder una
// semana. Elegir mal el período muestra las cifras de otros días sin avisar.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IndicadoresViewModelTest {

    private lateinit var vm: IndicadoresViewModel

    @Before
    fun preparar() {
        val application: Application = ApplicationProvider.getApplicationContext()
        vm = IndicadoresViewModel(application)
    }

    @Test
    fun onPeriodo_enHabiles_muestraElTipoDeDiaHabil() {
        vm.onPeriodo(TipoPeriodo.HABILES)

        assertEquals("Habil", vm.uiState.value.tipoDia)
    }

    @Test
    fun onSemanaAnterior_retrocedeUnaSemana() {
        vm.onPeriodo(TipoPeriodo.HABILES)
        val antes = vm.uiState.value.posicionSemana

        vm.onSemanaAnterior()

        assertEquals(antes + 1, vm.uiState.value.posicionSemana)
    }
}
