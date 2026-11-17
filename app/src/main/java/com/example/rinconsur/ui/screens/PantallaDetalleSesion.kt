package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.DetalleSesion
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.ui.components.BloquesConteo
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Superficie
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.DetalleSesionViewModel

@Composable
fun PantallaDetalleSesion(idSesion: Long, onRechazar: (Long) -> Unit) {
    val vm: DetalleSesionViewModel = viewModel()
    val detalle by vm.detalle.collectAsStateWithLifecycle()

    LaunchedEffect(idSesion) { vm.cargar(idSesion) }
    val ciclo = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.recargar(idSesion)
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    var confirmar by remember { mutableStateOf(false) }
    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.aviso.collect { host.showSnackbar(it) } }

    val d = detalle
    Scaffold(containerColor = Fondo, snackbarHost = { SnackbarHost(host) }) { pad ->
        Box(Modifier.padding(pad).fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Dimens.espacioPantalla)
                    .padding(bottom = if (d != null && vm.puedeRevisar()) 96.dp else Dimens.espacioGrande)
            ) {
                if (d == null) {
                    Nota("No se encontró la sesión.")
                    return@Column
                }

                Seccion("Resumen")
                TarjetaSeccion {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                d.sesion.codigo,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Tinta
                            )
                            Text(
                                d.sesion.fecha + " · " + d.sesion.horaInicioCorta + "–" + d.sesion.horaFinCorta,
                                style = MaterialTheme.typography.bodySmall,
                                color = Apagado
                            )
                            Text(
                                d.sesion.nombreObservador.ifEmpty { "Sin observador" },
                                style = MaterialTheme.typography.bodySmall,
                                color = Apagado
                            )
                        }
                        EtiquetaEstado(
                            d.sesion.estado.etiqueta,
                            when (d.sesion.estado) {
                                EstadoSesion.VALIDADA -> "ok"
                                EstadoSesion.RECHAZADA -> "aviso"
                                EstadoSesion.FINALIZADA -> "marca"
                                EstadoSesion.EN_CURSO -> "info"
                                else -> "neutro"
                            }
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    FilaDato("Registros válidos", d.registrosValidos.toString())
                    if (d.anulados > 0) FilaDato("Anulados", d.anulados.toString())
                    if (d.horaPrimera != null && d.horaUltima != null) {
                        FilaDato("Medición", d.horaPrimera + " a " + d.horaUltima)
                    }
                    d.duracionRealMinutos?.let { FilaDato("Duración medida", it.toString() + " min") }
                    val pct = d.porClasificacion.firstOrNull { it.etiqueta == "Tránsito" }?.porcentajeTexto
                    FilaDato("Tránsito", pct ?: "sin dato")
                }

                if (d.sinRegistros) {
                    Spacer(Modifier.height(Dimens.espacioChico))
                    TarjetaSeccion {
                        Text(
                            "Sin registros medidos en esta sesión.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Tinta
                        )
                        Nota("No hay segmentación que mostrar.")
                    }
                } else {
                    Seccion("Clasificación")
                    TarjetaSeccion { BloquesConteo(d.porClasificacion) }

                    Seccion("Rango etario")
                    TarjetaSeccion { BloquesConteo(d.porRango) }

                    Seccion("Compañía")
                    TarjetaSeccion { BloquesConteo(d.porCompania) }

                    Seccion("Compras en otra tienda")
                    TarjetaSeccion { BloquesConteo(d.comprasOtraTienda) }

                    if (d.destinosTransito.isNotEmpty()) {
                        Seccion("Destino del tránsito")
                        TarjetaSeccion { BloquesConteo(d.destinosTransito) }
                    }
                }

                d.sesion.motivoRechazo?.let {
                    Spacer(Modifier.height(Dimens.espacioChico))
                    Nota("Motivo del rechazo: " + it)
                }
            }

            if (d != null && d.sesion.estado.esperaRevision) {
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Superficie)
                        .padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
                ) {
                    if (vm.puedeRevisar()) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            BotonPrincipal(
                                texto = "Validar",
                                onClick = { confirmar = true },
                                modifier = Modifier.weight(1f)
                            )
                            BotonSecundario(
                                texto = "Rechazar",
                                onClick = { onRechazar(d.sesion.id) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    } else {
                        val texto = when {
                            d.sesion.idObservador > 0 && !d.sesion.sePuedeValidar ->
                                "Faltan registros para validar: mínimo 30 y hay " + d.sesion.registrosValidos + "."
                            else -> "Esta sesión la revisa otra persona."
                        }
                        Nota(texto)
                    }
                }
            }
        }
    }

    if (confirmar && d != null) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Validar " + d.sesion.codigo) },
            text = { Text("Queda validada con " + d.registrosValidos + " registros. No se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.validar(d.sesion.id, d.sesion.codigo)
                    confirmar = false
                }) { Text("Validar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(etiqueta, style = MaterialTheme.typography.bodySmall, color = Apagado)
        Text(
            valor,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Tinta
        )
    }
}
