package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.data.SesionPreferencias
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class RegistroViewModelTest {

    private lateinit var application: Application

    @Before
    fun preparar() {
        application = ApplicationProvider.getApplicationContext()

        SesionPreferencias(application).borrar()
    }

    @Test
    fun guardar_sinSesionEnCurso_noGuardaYExplicaPorQue() {
        val vm = RegistroViewModel(application)

        val guardado = vm.guardar()

        assertFalse(guardado)
        assertEquals("No tienes una sesión de conteo en curso.", vm.uiState.value.mensaje)
    }

    @Test
    fun aplicarPlantilla_sinPlantillaGuardada_noAplicaNada() {
        val vm = RegistroViewModel(application)

        val aplicada = vm.aplicarPlantilla(1)

        assertFalse(aplicada)
    }
}
