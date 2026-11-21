package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.model.PerfilUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// El login es la primera puerta de la app: si deja pasar un RUT mal escrito o la clave de otra
// persona, todo lo que sigue queda mal atribuido.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoginViewModelTest {

    private lateinit var vm: LoginViewModel

    @Before
    fun preparar() {
        val application: Application = ApplicationProvider.getApplicationContext()
        vm = LoginViewModel(application)
    }

    @Test
    fun entrar_conRutInvalido_muestraElErrorDelRutYNoAbreSesion() {
        vm.onRutCambia("12.345")
        vm.onClaveCambia("rincon2026")

        val perfil = vm.entrar()

        assertNull(perfil)
        assertNotNull("El mensaje del RUT queda a la vista", vm.uiState.value.errorRut)
    }

    @Test
    fun entrar_conClaveEquivocada_muestraElErrorGeneralYNoAbreSesion() {
        vm.onRutCambia("15.234.871-1")
        vm.onClaveCambia("claveQueNoEs")

        val perfil = vm.entrar()

        assertNull(perfil)
        assertEquals("RUT o contraseña incorrectos.", vm.uiState.value.errorGeneral)
    }

    @Test
    fun entrar_conCredencialesCorrectas_entraConElPerfilDeLaJefa() {
        vm.onRutCambia("15.234.871-1")
        vm.onClaveCambia("rincon2026")

        val perfil = vm.entrar()

        assertEquals(PerfilUsuario.JEFE_TIENDA, perfil)
    }
}
