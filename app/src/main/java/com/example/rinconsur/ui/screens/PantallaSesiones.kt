package com.example.rinconsur.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.style.TextAlign

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
import androidx.compose.runtime.LaunchedEffect
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

    // Al volver del formulario de rechazo se relee la lista: el rechazo se guarda desde otra pantalla.
    LaunchedEffect(Unit) { vm.recargar() }

    var abrirCalendario by remember { mutableStateOf(false) }

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
            // El dia que se revisa: flechas a los lados y el nombre del dia abre el calendario.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = vm::diaAnterior) {
                    Icon(
                        Icons.Filled.KeyboardArrowLeft,
                        contentDescription = "Día anterior",
                        tint = Tinta
                    )
                }
                Text(
                    text = if (estado.fechaLarga.isEmpty()) "Elegir día" else estado.fechaLarga,
                    style = MaterialTheme.typography.titleSmall,
                    color = Tinta,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { abrirCalendario = true }
                        .padding(vertical = 10.dp)
                )
                IconButton(onClick = vm::diaSiguiente) {
                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = "Día siguiente",
                        tint = Tinta
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // FilaFiltros ocupa todo el ancho por dentro: se le da el peso para que el atajo "Hoy"
                // conserve su lugar a la derecha.
                FilaFiltros(
                    opciones = listOf("Día", "Por validar"),
                    elegido = estado.filtro,
                    modifier = Modifier.weight(1f)
                ) { vm.filtrar(it) }
                TextButton(onClick = vm::irAHoy) {
                    Text("Hoy", style = MaterialTheme.typography.labelLarge, color = Apagado)
                }
            }

            // Lo que espera revision puede ser de otro dia: si no se dice, la lista de "Hoy" parece quieta.
            if (estado.porValidar.isNotEmpty() && estado.filtro != "Por validar") {
                Nota(
                    "Hay " + estado.porValidar.size +
                            if (estado.porValidar.size == 1) " sesión esperando revisión: está en el filtro Por validar."
                            else " sesiones esperando revisión: están en el filtro Por validar."
                )
            }

            if (estado.bloquesPendientes > 0) {
                Nota(
                    "Bloques por medir: " + estado.bloquesPendientes +
                            if (estado.pendienteMasAtrasado.isNotEmpty()) " · el más atrasado: " + estado.pendienteMasAtrasado else ""
                )
            }

            Spacer(Modifier.height(8.dp))
            BotonSecundario("Programar sesión", onClick = onProgramar, modifier = Modifier.fillMaxWidth())

            Seccion(
                if (estado.filtro == "Por validar")
                    "Por validar · " + estado.visibles.size +
                        if (estado.visibles.size == 1) " sesión" else " sesiones"
                else "Sesiones del día · " + estado.visibles.size
            )
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
    // El calendario sirve solo para elegir el dia: se elige y se vuelve a la lista de ese dia.
    if (abrirCalendario) {
        val formatoDia = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        val estadoDia = rememberDatePickerState(
            initialSelectedDateMillis = formatoDia.parse(estado.fecha)?.time
        )

        DatePickerDialog(
            onDismissRequest = { abrirCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoDia.selectedDateMillis?.let { milisegundos ->
                        vm.elegirFecha(formatoDia.format(java.util.Date(milisegundos)))
                    }
                    abrirCalendario = false
                }) { Text("Elegir") }
            },
            dismissButton = {
                TextButton(onClick = { abrirCalendario = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = estadoDia)
        }
    }

}
