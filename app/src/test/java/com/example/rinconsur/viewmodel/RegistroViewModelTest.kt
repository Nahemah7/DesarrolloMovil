package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.rinconsur.data.Plantilla
import com.example.rinconsur.data.Plantillas
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.RangoEtario
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// Registrar es un toque por persona en el pasillo: no hay tiempo de corregir. El ViewModel tiene
// que negarse a guardar un registro incompleto (uno que después contamine el tránsito) y tiene que
// dejar listo el toque siguiente con las plantillas.
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
    fun guardar_sinSesionEnCurso_noRegistraYAvisaPorQue() {
        val vm = RegistroViewModel(application)

        val guardado = vm.guardar()

        assertFalse("Sin sesión en curso no se puede anotar a nadie", guardado)
        assertEquals("No tienes una sesión de conteo en curso.", vm.uiState.value.mensaje)
        assertTrue(
            "La pantalla se marca sin sesión para no ofrecer botones que no funcionan",
            vm.uiState.value.sinSesionEnCurso
        )
    }

    @Test
    fun aplicarPlantilla_sinPlantillaGuardada_devuelveFalseYNoTocaElFormulario() {
        val vm = RegistroViewModel(application)

        val aplicada = vm.aplicarPlantilla(1)

        assertFalse("No hay plantilla 1 guardada en este teléfono", aplicada)
        assertNull("El formulario sigue vacío", vm.uiState.value.rangoEtario)
    }

    @Test
    fun aplicarPlantilla_guardadaPreviamente_cargaElToqueCompleto() {
        Plantillas(application).guardar(
            1,
            Plantilla(
                clasificacion = Clasificacion.VISITA,
                rangoEtario = RangoEtario.DE_26_A_40,
                compania = Compania.PAREJA,
                comprasOtraTienda = true,
                destino = null
            )
        )
        val vm = RegistroViewModel(application)
        vm.recargarPlantillas()

        val aplicada = vm.aplicarPlantilla(1)

        assertTrue("La plantilla se aplica de una vez", aplicada)
        val estado = vm.uiState.value
        assertEquals(Clasificacion.VISITA, estado.clasificacion)
        assertEquals(RangoEtario.DE_26_A_40, estado.rangoEtario)
        assertEquals(Compania.PAREJA, estado.compania)
        assertTrue("La plantilla trae también si compró en otra tienda", estado.comprasOtraTienda)
        assertNotNull("Se confirma qué plantilla se cargó", estado.mensaje)
    }

    @Test
    fun aplicarPlantilla_deTransitoSinDestino_noDejaElRegistroListoParaGuardar() {
        // Una plantilla de tránsito sin destino es media plantilla: el registro no puede salir así.
        Plantillas(application).guardar(
            2,
            Plantilla(
                clasificacion = Clasificacion.TRANSITO,
                rangoEtario = RangoEtario.DE_18_A_25,
                compania = Compania.SOLO,
                comprasOtraTienda = false,
                destino = null
            )
        )
        val vm = RegistroViewModel(application)
        vm.recargarPlantillas()

        vm.aplicarPlantilla(2)

        assertFalse(
            "El tránsito necesita destino antes de poder guardarse",
            vm.uiState.value.puedeGuardar
        )
    }

    @Test
    fun onDestino_enTransito_dejaElRegistroListoParaGuardar() {
        val vm = RegistroViewModel(application)
        vm.onClasificacion(Clasificacion.TRANSITO)
        vm.onRangoEtario(RangoEtario.DE_41_A_60)

        assertFalse("Todavía falta decir hacia dónde va", vm.uiState.value.puedeGuardar)

        vm.onDestino(2)

        assertTrue(
            "Con clasificación, rango y destino el toque queda completo",
            vm.uiState.value.puedeGuardar
        )
    }
}
