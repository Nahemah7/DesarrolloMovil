package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.model.PerfilUsuario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// El inicio de sesión es la primera puerta de la app: si deja pasar a alguien con el RUT mal
// escrito o con la clave de otra persona, todo lo que sigue queda mal atribuido. Estas pruebas
// entran por el ViewModel, que es donde vive la decisión, no por la pantalla.
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LoginViewModelTest {

    private lateinit var application: Application
    private lateinit var vm: LoginViewModel

    @Before
    fun preparar() {
        application = ApplicationProvider.getApplicationContext()
        vm = LoginViewModel(application)
    }

    @Test
    fun entrar_conRutInvalido_noAbreSesionYLoDice() {
        vm.onRutCambia("12.345")
        vm.onClaveCambia("rincon2026")

        val perfil = vm.entrar()

        assertNull("Un RUT incompleto no puede abrir sesión", perfil)
        assertNotNull("Se tiene que explicar qué falta", vm.uiState.value.errorRut)
        assertNull(vm.uiState.value.errorGeneral)
    }

    @Test
    fun entrar_conClaveVacia_pideLaClaveYNoAbreSesion() {
        vm.onRutCambia("15.234.871-1")
        vm.onClaveCambia("")

        val perfil = vm.entrar()

        assertNull(perfil)
        assertEquals("Escribe tu contraseña", vm.uiState.value.errorClave)
    }

    @Test
    fun entrar_conClaveEquivocada_noDiceCualDeLosDosDatosEstabaMal() {
        vm.onRutCambia("15.234.871-1")
        vm.onClaveCambia("claveQueNoEs")

        val perfil = vm.entrar()

        assertNull(perfil)
        // El mensaje no distingue RUT de clave: decir cuál existe es regalar media credencial.
        assertEquals("RUT o contraseña incorrectos.", vm.uiState.value.errorGeneral)
        assertNull(vm.uiState.value.errorRut)
    }

    @Test
    fun entrar_conCredencialesCorrectas_abreLaSesionDelPerfilQueCorresponde() {
        vm.onRutCambia("15.234.871-1")
        vm.onClaveCambia("rincon2026")

        val perfil = vm.entrar()

        assertEquals("La jefa de tienda entra como jefa", PerfilUsuario.JEFE_TIENDA, perfil)
        assertNotNull("El colaborador queda cargado para el resto de la app", vm.usuario.value)
        assertNull(vm.uiState.value.errorGeneral)
    }

    @Test
    fun entrar_conOtroPerfil_devuelveElPerfilDelColaborador() {
        vm.onRutCambia("19.876.345-4")
        vm.onClaveCambia("rincon2026")

        val perfil = vm.entrar()

        assertEquals(PerfilUsuario.OBSERVADOR, perfil)
        assertTrue("El nombre viene del registro, no del formulario", vm.usuario.value!!.nombre.isNotBlank())
    }
}
