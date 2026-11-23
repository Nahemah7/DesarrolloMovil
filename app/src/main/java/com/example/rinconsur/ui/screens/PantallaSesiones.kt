package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.FuenteDatos
import androidx.compose.ui.draw.clip
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.ui.components.EtiquetaFuente
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.FilaFiltros
import com.example.rinconsur.ui.components.Filtro
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.SesionesViewModel

private fun fechaDiaMes(fecha: String): String {
    val p = fecha.split("-")
    return if (p.size == 3) p[2] + "-" + p[1] else fecha
}

@Composable
fun PantallaSesiones(onRechazar: (Long) -> Unit, onProgramar: () -> Unit, onAbrir: (Long) -> Unit) {
    val vm: SesionesViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.recargar() }

    var abrirCalendario by remember { mutableStateOf(false) }
    var porValidar by remember { mutableStateOf<SesionResumen?>(null) }

    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.aviso.collect { host.showSnackbar(it) } }

    Scaffold(containerColor = Fondo, snackbarHost = { SnackbarHost(host) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = vm::diaAnterior) {
                    Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "Día anterior", tint = Tinta)
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
                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Día siguiente", tint = Tinta)
                }
            }

            if (estado.fuente != FuenteDatos.EN_LINEA) {
                EtiquetaFuente(estado.fuente)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilaFiltros(
                    opciones = listOf("Por validar", "Día", "Semana"),
                    elegido = estado.filtro,
                    modifier = Modifier.weight(1f)
                ) { vm.filtrar(it) }
                TextButton(onClick = vm::irAHoy) {
                    Text("Hoy", style = MaterialTheme.typography.labelLarge, color = Apagado)
                }
            }

            TarjetaSeccion {
                Text(
                    text = "Filtros",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Apagado
                )
                Spacer(Modifier.height(6.dp))
                OutlinedTextField(
                    value = estado.busquedaObservador,
                    onValueChange = vm::buscarObservador,
                    singleLine = true,
                    label = { Text("Buscar por observador o sesión") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                FilaFiltros(
                    opciones = listOf("Todos") + vm.accesos.map { it.codigo },
                    elegido = vm.accesos.firstOrNull { it.id == estado.idAcceso }?.codigo ?: "Todos"
                ) { elegido ->
                    vm.filtrarAcceso(vm.accesos.firstOrNull { it.codigo == elegido }?.id)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Filtro(texto = "Solo las mías", activo = estado.soloMias, onClick = vm::alternarSoloMias)
                    if (estado.hayFiltrosActivos) {
                        TextButton(onClick = vm::limpiarFiltros) {
                            Text("Limpiar", style = MaterialTheme.typography.labelMedium, color = Apagado)
                        }
                    }
                }
            }

            if (estado.porValidar.isNotEmpty()) {
                val ordenadas = estado.porValidar.sortedByDescending { it.horasEsperando() ?: 0L }
                Seccion("Recordatorios")
                TarjetaSeccion {
                    Text(
                        ordenadas.size.toString() + if (ordenadas.size == 1) " sesión espera revisión"
                        else " sesiones esperan revisión",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Tinta
                    )
                    Spacer(Modifier.height(6.dp))
                    ordenadas.take(3).forEach { pendiente ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .clickable { onAbrir(pendiente.id) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(pendiente.codigo, style = MaterialTheme.typography.bodyMedium, color = Tinta)
                                Text(
                                    pendiente.nombreObservador,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Apagado
                                )
                            }
                            Text(
                                pendiente.esperaTexto() ?: "",
                                style = MaterialTheme.typography.labelSmall,
                                color = Aviso
                            )
                        }
                    }
                }
            }

            if (estado.bloquesPendientes > 0) {
                Nota(
                    "Bloques por medir: " + estado.bloquesPendientes +
                        if (estado.pendienteMasAtrasado.isNotEmpty()) " · el más atrasado: " + estado.pendienteMasAtrasado else ""
                )
            }

            Spacer(Modifier.height(8.dp))
            BotonSecundario("Programar sesión", onClick = onProgramar, modifier = Modifier.fillMaxWidth())

            val titulo = when (estado.filtro) {
                "Por validar" -> "Por validar · " + estado.visibles.size +
                    if (estado.visibles.size == 1) " sesión" else " sesiones"
                "Semana" -> "Últimos 7 días · " + estado.visibles.size
                else -> "Sesiones del día · " + estado.visibles.size
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Seccion(titulo)
                Spacer(Modifier.weight(1f))

                EtiquetaFuente(estado.fuente)
            }

            if (estado.visibles.isEmpty()) {
                TarjetaSeccion { Nota("No hay sesiones con estos filtros.") }
            }

            estado.visibles.forEach { s ->
                val acceso = vm.accesos.firstOrNull { it.id == s.idAcceso }?.codigo ?: ""
                Spacer(Modifier.height(8.dp))
                TarjetaSesion(
                    sesion = s,
                    acceso = acceso,
                    esMia = s.idObservador == estado.miId,
                    onAbrir = { onAbrir(s.id) },
                    onValidar = { porValidar = s },
                    onRechazar = { onRechazar(s.id) }
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }

    porValidar?.let { s ->
        AlertDialog(
            onDismissRequest = { porValidar = null },
            title = { Text("Validar " + s.codigo) },
            text = {
                Text(
                    "Queda validada con " + s.registrosValidos + " registros" +
                        (s.pctTransito?.let { " y " + String.format("%.1f", it) + " % de tránsito" } ?: "") +
                        ". No se puede deshacer."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.validar(s.id, s.codigo)
                    porValidar = null
                }) { Text("Validar") }
            },
            dismissButton = {
                TextButton(onClick = { porValidar = null }) { Text("Cancelar") }
            }
        )
    }

    if (abrirCalendario) {
        val formatoDia = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
        val estadoDia = rememberDatePickerState(initialSelectedDateMillis = formatoDia.parse(estado.fecha)?.time)

        DatePickerDialog(
            onDismissRequest = { abrirCalendario = false },
            confirmButton = {
                TextButton(onClick = {
                    estadoDia.selectedDateMillis?.let { ms -> vm.elegirFecha(formatoDia.format(java.util.Date(ms))) }
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

@Composable
private fun TarjetaSesion(
    sesion: SesionResumen,
    acceso: String,
    esMia: Boolean,
    onAbrir: () -> Unit,
    onValidar: () -> Unit,
    onRechazar: () -> Unit
) {
    TarjetaSeccion(modifier = Modifier.clickable { onAbrir() }) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    sesion.codigo + if (acceso.isEmpty()) "" else " · " + acceso,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Tinta
                )
                Text(
                    fechaDiaMes(sesion.fecha) + " · " + sesion.horaInicioCorta + "–" + sesion.horaFinCorta,
                    style = MaterialTheme.typography.bodySmall,
                    color = Apagado
                )
                Text(
                    sesion.nombreObservador.ifEmpty { "Sin observador" },
                    style = MaterialTheme.typography.bodySmall,
                    color = Apagado
                )
            }
            if (sesion.estado == EstadoSesion.FINALIZADA && sesion.registrosValidos >= 30) {
                sesion.esperaTexto()?.let { espera ->
                    EtiquetaEstado("Esperando validación " + espera, "aviso")
                }
            }
            if (sesion.sinPublicar) {
                EtiquetaEstado("Sin publicar", "aviso")
            }
            EtiquetaEstado(
                sesion.estado.etiqueta,
                when (sesion.estado) {
                    EstadoSesion.VALIDADA -> "ok"
                    EstadoSesion.RECHAZADA -> "aviso"
                    EstadoSesion.FINALIZADA -> "marca"
                    EstadoSesion.EN_CURSO -> "info"
                    else -> "neutro"
                }
            )
        }

        if (sesion.sinPublicar) {
            Text(
                text = "Esta sesión existe solo en este teléfono: se enviará al servidor en cuanto haya " +
                    "conexión y sesión iniciada.",
                style = MaterialTheme.typography.labelSmall,
                color = Apagado
            )
        }

        if (sesion.registrosValidos > 0) {
            val pct = sesion.pctTransito?.let { String.format("%.1f %%", it) } ?: "sin dato"
            Text(
                text = sesion.registrosValidos.toString() + " registros · tránsito " + pct,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Tinta
            )
        }
        sesion.motivoRechazo?.let { Nota("Motivo del rechazo: " + it) }

        if (sesion.estado.esperaRevision) {
            when {
                esMia -> Nota("Es tu propia sesión: la revisa otra persona.")
                else -> {
                    if (!sesion.sePuedeValidar) {
                        Nota("Faltan registros para validar: mínimo 30 y hay " + sesion.registrosValidos + ".")
                    }
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonPrincipal(
                            texto = "Validar",
                            onClick = onValidar,
                            modifier = Modifier.weight(1f),
                            habilitado = sesion.sePuedeValidar
                        )
                        BotonSecundario(
                            texto = "Rechazar",
                            onClick = onRechazar,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
