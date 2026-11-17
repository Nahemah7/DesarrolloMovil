package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.HoraPunta
import com.example.rinconsur.model.ReporteJefatura
import com.example.rinconsur.ui.components.BloquesConteo
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.ReporteViewModel

@Composable
fun PantallaReporte() {
    val vm: ReporteViewModel = viewModel()
    val reporte by vm.reporte.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.cargar() }
    val ciclo = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.cargar()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    var pidiendoMeta by remember { mutableStateOf(false) }
    var textoMeta by remember { mutableStateOf("") }
    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.aviso.collect { host.showSnackbar(it) } }

    Scaffold(containerColor = Fondo, snackbarHost = { SnackbarHost(host) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            val r = reporte
            if (r == null) {
                Nota("Cargando el reporte.")
                return@Column
            }

            Seccion("Meta del mes")
            TarjetaSeccion {
                if (r.meta.registrada) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(
                                "Meta de conversión",
                                style = MaterialTheme.typography.bodySmall,
                                color = Apagado
                            )
                            Text(
                                r.meta.objetivo?.let { com.example.rinconsur.model.formatoPorcentaje(it) } ?: "",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Marino
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Avance", style = MaterialTheme.typography.bodySmall, color = Apagado)
                            Text(
                                r.meta.cumplimientoTexto,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Tinta
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(Linea)) {
                        Box(Modifier.fillMaxWidth(r.meta.avance).height(8.dp).clip(RoundedCornerShape(4.dp)).background(Aqua))
                    }
                    Spacer(Modifier.height(6.dp))
                    Nota("Conversión medida del mes: " + (r.meta.actual?.let { com.example.rinconsur.model.formatoPorcentaje(it) } ?: "sin información") +
                        " · " + r.meta.dias + if (r.meta.dias == 1) " día con datos" else " días con datos")
                } else {
                    Nota("Sin meta registrada para este mes.")
                    Nota("Conversión medida: " + (r.meta.actual?.let { com.example.rinconsur.model.formatoPorcentaje(it) } ?: "sin información"))
                }
                if (vm.puedeRegistrarMeta) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = {
                        textoMeta = r.meta.objetivo?.toString()?.replace(".", ",") ?: ""
                        pidiendoMeta = true
                    }) { Text(if (r.meta.registrada) "Cambiar la meta" else "Registrar la meta") }
                }
            }

            Seccion("Comparación por mes")
            TarjetaSeccion {
                if (r.meses.isEmpty()) {
                    Nota("Sin datos de conversión cargados.")
                } else {
                    r.meses.forEach { mes ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(mes.periodo, style = MaterialTheme.typography.bodySmall, color = Tinta)
                            Text(
                                mes.conversionTexto + " · " + mes.dias +
                                    if (mes.dias == 1) " día" else " días",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Tinta
                            )
                        }
                        Text(
                            mes.ingresos.toString() + " ingresos · " + mes.boletas + " boletas",
                            style = MaterialTheme.typography.labelSmall,
                            color = Apagado
                        )
                    }
                }
            }

            Seccion("Horas punta")
            TarjetaSeccion {
                if (r.horas.isEmpty()) {
                    Nota("Sin ingresos cargados para calcular horas punta.")
                } else {
                    r.horaPunta?.let { punta ->
                        Nota("Mayor tránsito medido: " + punta.hora + " con " + punta.transito + " personas.")
                    }
                    r.horas.forEach { hora -> FilaHora(hora, esPunta = hora.hora == r.horaPunta?.hora) }
                    Nota("Solo las horas con sesión validada tienen tránsito medido.")
                }
            }

            Seccion("Perfil del tránsito")
            TarjetaSeccion {
                Text("Por rango de edad", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Apagado)
                Spacer(Modifier.height(4.dp))
                BloquesConteo(r.porRango)
                Spacer(Modifier.height(12.dp))
                Text("Por compañía", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Apagado)
                Spacer(Modifier.height(4.dp))
                BloquesConteo(r.porCompania)
                Spacer(Modifier.height(12.dp))
                Text("Compra en otra tienda", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = Apagado)
                Spacer(Modifier.height(4.dp))
                BloquesConteo(r.compras)
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }

    if (pidiendoMeta && reporte != null) {
        val meta = reporte!!.meta
        AlertDialog(
            onDismissRequest = { pidiendoMeta = false },
            title = { Text("Meta de conversión") },
            text = {
                Column {
                    Text("Porcentaje objetivo del mes en curso (0 a 100).")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = textoMeta,
                        onValueChange = { textoMeta = it },
                        label = { Text("Porcentaje") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (vm.registrarMeta(textoMeta)) pidiendoMeta = false
                }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { pidiendoMeta = false }) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun FilaHora(hora: HoraPunta, esPunta: Boolean) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            hora.hora + if (esPunta) "  ·  punta" else "",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (esPunta) FontWeight.SemiBold else FontWeight.Normal,
            color = if (esPunta) Marino else Tinta
        )
        Text(
            if (hora.medido) hora.transito.toString() + " medidos · " + hora.ingresos + " ingresos"
            else hora.ingresos.toString() + " ingresos · sin medir",
            style = MaterialTheme.typography.bodySmall,
            color = if (hora.medido) Tinta else Apagado
        )
    }
}
