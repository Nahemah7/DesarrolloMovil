package com.example.rinconsur

import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.model.SesionesUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SesionesFiltrosTest {

    private fun sesion(
        id: Long,
        idAcceso: Long,
        idObservador: Long,
        idProgramador: Long = 3,
        estado: EstadoSesion = EstadoSesion.FINALIZADA
    ) = SesionResumen(
        id = id,
        codigo = "S-000" + id,
        idAcceso = idAcceso,
        fecha = "2026-09-15",
        horaInicioProg = "13:00:00",
        horaFinProg = "13:45:00",
        estado = estado,
        idObservador = idObservador,
        idProgramador = idProgramador
    )

    private val pendientes = listOf(
        sesion(1, idAcceso = 1, idObservador = 5),
        sesion(2, idAcceso = 2, idObservador = 6),
        sesion(3, idAcceso = 2, idObservador = 3)
    )

    @Test
    fun visibles_sinFiltros_muestraSoloLasPendientes() {
        val estado = SesionesUiState(porValidar = pendientes, miId = 3)

        assertEquals(3, estado.visibles.size)
        assertTrue(estado.visibles.all { it.estado == EstadoSesion.FINALIZADA })
    }

    @Test
    fun visibles_conAccesoElegido_filtraPorEseAcceso() {
        val estado = SesionesUiState(porValidar = pendientes, miId = 3, idAcceso = 2)

        assertEquals(2, estado.visibles.size)
        assertTrue(estado.visibles.all { it.idAcceso == 2L })
    }

    @Test
    fun visibles_conObservadorElegido_filtraPorEseObservador() {
        val estado = SesionesUiState(porValidar = pendientes, miId = 3, idObservador = 5)

        assertEquals(1, estado.visibles.size)
        assertEquals(5L, estado.visibles.first().idObservador)
    }

    @Test
    fun visibles_conSoloMias_muestraLasQueMediYo() {
        val estado = SesionesUiState(porValidar = pendientes, miId = 3, soloMias = true)

        assertEquals(1, estado.visibles.size)
        assertEquals(3L, estado.visibles.first().idObservador)
    }

    @Test
    fun hayFiltrosActivos_sinFiltros_esFalso() {
        assertFalse(SesionesUiState(porValidar = pendientes, miId = 3).hayFiltrosActivos)
    }

    @Test
    fun hayFiltrosActivos_conSoloMias_esVerdadero() {
        assertTrue(SesionesUiState(porValidar = pendientes, miId = 3, soloMias = true).hayFiltrosActivos)
    }

    @Test
    fun visibles_enFiltroDia_usaLaListaDelDia() {
        val estado = SesionesUiState(
            sesiones = listOf(sesion(9, idAcceso = 4, idObservador = 7)),
            porValidar = pendientes,
            filtro = "Día",
            miId = 3
        )

        assertEquals(1, estado.visibles.size)
        assertEquals(9L, estado.visibles.first().id)
    }
}
