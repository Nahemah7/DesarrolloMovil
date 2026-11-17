package com.example.rinconsur

import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.ui.components.esRaiz
import com.example.rinconsur.ui.components.menuDe
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// La flecha de retroceso solo aparece fuera de las pestañas del perfil.
class BarraNavegacionTest {

    @Test
    fun cadaPestanaDelPerfilEsRaiz() {
        PerfilUsuario.entries.forEach { perfil ->
            menuDe(perfil).forEach { opcion ->
                assertTrue(
                    perfil.etiqueta + " / " + opcion.etiqueta + " deberia ser raiz",
                    esRaiz(opcion.ruta, menuDe(perfil))
                )
            }
        }
    }

    @Test
    fun cargaEsRaizParaElSubgerente() {
        assertTrue(esRaiz("carga", menuDe(PerfilUsuario.SUBGERENTE)))
    }

    @Test
    fun reporteEsRaizParaLaJefatura() {
        assertTrue(esRaiz("reporte", menuDe(PerfilUsuario.JEFE_TIENDA)))
    }

    @Test
    fun elDetalleDeUnaSesionNoEsRaiz() {
        assertFalse(esRaiz("sesion/5", menuDe(PerfilUsuario.SUPERVISOR)))
    }

    @Test
    fun elAltaDeUnaPersonaNoEsRaiz() {
        assertFalse(esRaiz("nueva_persona", menuDe(PerfilUsuario.ADMINISTRADOR)))
    }

    @Test
    fun elLoginNoEsRaiz() {
        PerfilUsuario.entries.forEach { perfil ->
            assertFalse(esRaiz("login", menuDe(perfil)))
        }
    }
}
