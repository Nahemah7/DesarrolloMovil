package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.FilaFiltros
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.SesionesViewModel

/** "2026-09-15" se muestra como 15-09. */
private fun fechaDiaMes(fecha: String): String {
    val p = fecha.split("-")
    return if (p.size == 3) p[2] + "-" + p[1] else fecha
}

/**
 * Lista de sesiones del supervisor (filtros Hoy, Por validar y Semana). Las sesiones finalizadas
 * se pueden validar o rechazar. Validar se hace aquí (vm.validar); rechazar abre el formulario
 * de motivos (PantallaRechazar) a través de [onRechazar].
 *
 * Reglas que cuida esta pantalla: una sesión necesita al menos 30 registros válidos para validarse
 * (RN-10) y quien la observó no puede revisarla (HU-05).
 */
@Composable
fun PantallaSesiones(onRechazar: (Long) -> Unit, onProgramar: () -> Unit) {
    val vm: SesionesViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    // Quién está mirando la lista: para que nadie revise su propia sesión
    val contexto = LocalContext.current
    val idRevisor = remember { SesionPreferencias(contexto).leer() }

    Scaffold(containerColor = Hueso) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Seccion("Filtro")
            FilaFiltros(
                opciones = listOf("Hoy", "Por validar", "Semana"),
                elegido = estado.filtro
            ) { vm.filtrar(it) }

            if (estado.bloquesPendientes > 0) {
                Nota(
                    "Bloques por medir: " + estado.bloquesPendientes +
                            if (estado.pendienteMasAtrasado.isNotEmpty()) " · el más atrasado: " + estado.pendienteMasAtrasado else ""
                )
            }

            Spacer(Modifier.height(8.dp))
            BotonSecundario("Programar sesión", onClick = onProgramar, modifier = Modifier.fillMaxWidth())

            Seccion(estado.filtro + " · " + estado.visibles.size + " sesiones")
            if (estado.visibles.isEmpty()) {
                TarjetaSeccion { Nota("No hay sesiones en este filtro.") }
            }

            estado.visibles.forEach { s ->
                val acceso = vm.accesos.firstOrNull { it.id == s.idAcceso }?.etiquetaCorta ?: ""
                Spacer(Modifier.height(8.dp))
                TarjetaSeccion {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                s.codigo + " · " + acceso,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Tinta
                            )
                            Text(
                                fechaDiaMes(s.fecha) + " · " + s.horaInicioCorta + "–" + s.horaFinCorta +
                                        " · " + s.nombreObservador,
                                style = MaterialTheme.typography.bodySmall,
                                color = Apagado
                            )
                        }
                        // El estado va escrito, no solo con color
                        EtiquetaEstado(
                            s.estado.etiqueta,
                            when (s.estado) {
                                EstadoSesion.VALIDADA -> "ok"
                                EstadoSesion.RECHAZADA -> "aviso"
                                EstadoSesion.FINALIZADA -> "marca"
                                EstadoSesion.EN_CURSO -> "info"
                                else -> "neutro"
                            }
                        )
                    }

                    if (s.registrosValidos > 0) {
                        val pct = s.pctTransito?.let { String.format("%.1f %%", it) } ?: "sin dato"
                        Nota(s.registrosValidos.toString() + " registros · tránsito " + pct)
                    }
                    s.motivoRechazo?.let { Nota("Motivo del rechazo: " + it) }

                    // Solo las finalizadas esperan revisión
                    if (s.estado.esperaRevision) {
                        when {
                            s.idObservador == idRevisor ->
                                Nota("Es tu propia sesión: la revisa otra persona.")
                            else -> {
                                if (!s.sePuedeValidar) {
                                    Nota("Faltan registros para validar: mínimo 30 y hay " + s.registrosValidos + ".")
                                }
                                Spacer(Modifier.height(6.dp))
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    BotonPrincipal(
                                        texto = "Validar",
                                        onClick = { vm.validar(s.id) },
                                        modifier = Modifier.weight(1f),
                                        habilitado = s.sePuedeValidar
                                    )
                                    BotonSecundario(
                                        texto = "Rechazar",
                                        onClick = { onRechazar(s.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}