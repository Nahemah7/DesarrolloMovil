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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.DisposableEffect
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
import com.example.rinconsur.model.formatoPorcentaje
import com.example.rinconsur.ui.components.AnilloProgreso
import com.example.rinconsur.ui.components.BarrasVerticales
import com.example.rinconsur.ui.components.Donut
import com.example.rinconsur.ui.components.EtiquetaEjemplo
import com.example.rinconsur.ui.components.LeyendaGrafico
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.AquaTenue
import com.example.rinconsur.ui.theme.ColoresCompras
import com.example.rinconsur.ui.theme.SerieCategorica
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.ui.theme.colorDeRango
import com.example.rinconsur.viewmodel.ReporteViewModel

@Composable
fun PantallaReporte() {
    val vm: ReporteViewModel = viewModel()
    val reporte by vm.reporte.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.cargar() }
    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
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
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            val r = reporte
            if (r == null) {
                Nota("Cargando el reporte.")
                return@Column
            }

            if (r.deEjemplo) {
                EtiquetaEjemplo()
                Spacer(Modifier.height(Dimens.espacioMini))
            }

            Seccion("Meta del mes")
            TarjetaSeccion {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnilloProgreso(
                        avance = r.meta.avance,
                        texto = if (r.meta.registrada) r.meta.cumplimientoTexto else "—",
                        subtexto = if (r.meta.registrada) "de la meta" else "sin meta"
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        if (r.meta.registrada) {
                            Text(
                                text = "Meta " + r.meta.objetivo?.let { formatoPorcentaje(it) },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Marino
                            )
                        } else {
                            Text(
                                text = "Sin meta registrada",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = Marino
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = "Conversión medida: " +
                                (r.meta.actual?.let { formatoPorcentaje(it) } ?: "sin información"),
                            style = MaterialTheme.typography.bodySmall,
                            color = Tinta
                        )
                        Text(
                            text = r.meta.dias.toString() +
                                if (r.meta.dias == 1) " día con datos" else " días con datos",
                            style = MaterialTheme.typography.labelSmall,
                            color = Apagado
                        )
                        if (vm.puedeRegistrarMeta) {
                            TextButton(onClick = {
                                textoMeta = r.meta.objetivo?.toString()?.replace(".", ",") ?: ""
                                pidiendoMeta = true
                            }) {
                                Text(if (r.meta.registrada) "Cambiar la meta" else "Registrar la meta")
                            }
                        }
                    }
                }
            }

            Seccion("Comparación por mes")
            TarjetaSeccion {
                if (r.meses.isEmpty()) {
                    Nota("Sin datos de conversión cargados.")
                } else {
                    val meses = r.meses.reversed()
                    // Las barras se escalan con el entero, pero la etiqueta lleva el decimal real.
                    BarrasVerticales(
                        datos = meses.map { mes ->
                            nombreCorto(mes.mes) to (mes.conversion?.let { Math.round(it * 10).toInt() } ?: 0)
                        },
                        indiceDestacado = meses.lastIndex,
                        textos = meses.map { mes ->
                            mes.conversion?.let { String.format(java.util.Locale("es", "CL"), "%.1f", it) } ?: "—"
                        }
                    )
                    Nota("Conversión bruta de cada mes, en porcentaje: boletas sobre ingresos cargados.")
                    val ultimo = meses.last()
                    LeyendaGrafico(
                        listOf(
                            "Ingresos de " + ultimo.periodo to ultimo.ingresos.toString(),
                            "Boletas de " + ultimo.periodo to ultimo.boletas.toString(),
                            "Días con datos" to ultimo.dias.toString()
                        )
                    )
                }
            }

            Seccion("Horas punta")
            TarjetaSeccion {
                if (r.horas.isEmpty()) {
                    Nota("Sin ingresos cargados para calcular horas punta.")
                } else {
                    BarrasVerticales(
                        datos = r.horas.map { hora -> hora.hora to hora.transito },
                        indiceDestacado = r.horas.indexOfFirst { it.hora == r.horaPunta?.hora }.takeIf { it >= 0 }
                    )
                    if (r.horaPunta != null) {
                        Nota("Mayor tránsito medido: " + r.horaPunta!!.hora + " con " +
                            r.horaPunta!!.transito + " personas.")
                    } else {
                        Nota("Todavía no hay horas con tránsito medido: las barras aparecen cuando exista una sesión validada.")
                    }
                }
            }

            Seccion("Perfil del tránsito")
            TarjetaSeccion {
                val total = r.porRango.sumOf { it.cantidad }
                Donut(
                    datos = r.porRango.map { it.etiqueta to it.cantidad },
                    centro = total.toString(),
                    centroAbajo = "personas",
                    // Mismos colores que la plantilla del observador: el tramo se reconoce de un vistazo.
                    colores = r.porRango.map { colorDeRango(it.etiqueta) ?: Apagado }
                )
                Spacer(Modifier.height(4.dp))
                Nota("Rango de edad estimado de quienes pasaron.")
            }

            Spacer(Modifier.height(Dimens.espacioChico))
            TarjetaSeccion {
                Donut(
                    datos = r.porCompania.map { it.etiqueta to it.cantidad },
                    colores = SerieCategorica
                )
                Spacer(Modifier.height(4.dp))
                Nota("Con quién venía cada persona.")
            }

            Spacer(Modifier.height(Dimens.espacioChico))
            TarjetaSeccion {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(Dimens.radioEtiqueta))
                        .background(AquaTenue)
                        .padding(Dimens.espacioChico)
                ) {
                    Donut(
                        datos = r.compras.map { it.etiqueta to it.cantidad },
                        colores = ColoresCompras
                    )
                }
                Spacer(Modifier.height(4.dp))
                Nota("Si la persona compró o no en otra tienda antes de entrar.")
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }

    if (pidiendoMeta && reporte != null) {
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

// "2026-10" -> "oct" para que quepa debajo de la barra.
private fun nombreCorto(mes: String): String {
    val nombres = listOf(
        "ene", "feb", "mar", "abr", "may", "jun",
        "jul", "ago", "sep", "oct", "nov", "dic"
    )
    val indice = (mes.split("-").getOrNull(1)?.toIntOrNull() ?: 1) - 1
    return nombres.getOrNull(indice) ?: mes
}
