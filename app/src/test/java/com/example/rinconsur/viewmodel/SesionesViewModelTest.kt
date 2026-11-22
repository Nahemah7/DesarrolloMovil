package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SesionesViewModelTest {

    private lateinit var vm: SesionesViewModel

    @Before
    fun preparar() {
        val application: Application = ApplicationProvider.getApplicationContext()
        vm = SesionesViewModel(application)
    }

    @Test
    fun filtrar_porValidar_dejaPuestoElFiltroPorValidar() {
        vm.filtrar("Por validar")

        assertEquals("Por validar", vm.uiState.value.filtro)
    }

    @Test
    fun alternarSoloMias_prendeElFiltroDeMisSesiones() {
        vm.alternarSoloMias()

        assertTrue(vm.uiState.value.soloMias)
    }

    @Test
    fun elegirFecha_dejaLaVistaEnElDiaElegido() {
        vm.elegirFecha("2026-09-15")

        assertEquals("2026-09-15", vm.uiState.value.fecha)
    }
}
