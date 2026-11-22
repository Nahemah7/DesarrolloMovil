package com.example.rinconsur.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.TipoPeriodo
import com.example.rinconsur.ui.components.EtiquetaFuente
import com.example.rinconsur.ui.components.BarraPorcentaje
import com.example.rinconsur.ui.components.EtiquetaEjemplo
import com.example.rinconsur.ui.components.FilaFiltros
import com.example.rinconsur.ui.components.FilaKpis
import com.example.rinconsur.ui.components.KpiMediano
import com.example.rinconsur.ui.components.KpiPrincipal
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.IndicadoresViewModel

@Composable
fun PantallaIndicadores(onIrA: (String) -> Unit) {
    val vm: IndicadoresViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.recargar()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    val ind = state.indicadores
    val medicion = state.medicion
    val porAcceso = state.porAcceso
    val esHoy = state.periodo == TipoPeriodo.HOY
    val bloques = state.bloquesDelDia
    var detalleAbierto by remember { mutableStateOf(false) }

    val cabecera = if (esHoy) fechaCorta(state.fecha) + " · hoy"
        else {
            val dias = ind?.dias ?: medicion.dias
            state.periodo.etiqueta + " · " + dias + (if (dias == 1) " día" else " días") +
                (ind?.let { " (" + fechaCorta(it.desde) + " a " + fechaCorta(it.hasta) + ")" } ?: "")
        }
    val alcance = if (esHoy) "del día" else "del período"
    val maxIngresosAcceso = porAcceso.maxOfOrNull { it.ingresos }?.takeIf { it > 0 } ?: 1

    Scaffold(containerColor = Fondo) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            if (!esHoy && state.semanas.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.onSemanaAnterior() }, enabled = state.haySemanaAnterior) {
                        Icon(
                            Icons.Filled.KeyboardArrowLeft,
                            contentDescription = "Semana anterior",
                            tint = if (state.haySemanaAnterior) Tinta else Apagado
                        )
                    }
                    Text(
                        text = state.semanaElegida?.titulo ?: "Todas las semanas",
                        style = MaterialTheme.typography.titleSmall,
                        color = Tinta,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { vm.onSemanaSiguiente() }, enabled = state.haySemanaSiguiente) {
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            contentDescription = "Semana siguiente",
                            tint = if (state.haySemanaSiguiente) Tinta else Apagado
                        )
                    }
                }
            }

            FilaFiltros(
                opciones = TipoPeriodo.entries.map { it.etiqueta },
                elegido = state.periodo.etiqueta
            ) { etiqueta ->
                TipoPeriodo.entries.firstOrNull { it.etiqueta == etiqueta }?.let { vm.onPeriodo(it) }
            }
            Spacer(Modifier.height(Dimens.espacioChico))

            if (state.deEjemplo) {
                EtiquetaEjemplo()
                Spacer(Modifier.height(Dimens.espacioChico))
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = cabecera,
                    style = MaterialTheme.typography.labelSmall,
                    color = Apagado,
                    modifier = Modifier.weight(1f)
                )
                EtiquetaFuente(state.fuente)
            }

            Spacer(Modifier.height(Dimens.espacioChico))
            KpiPrincipal(
                valor = numeroAnimado(medicion.personas),
                etiqueta = "Personas medidas",
                contexto = if (esHoy) {
                    "en ${medicion.bloques} de $bloques bloques"
                } else {

                    "en ${medicion.bloques} " + (if (medicion.bloques == 1) "bloque con medición" else "bloques con medición")
                },
                avance = if (esHoy) medicion.bloques.toFloat() / bloques else null,
                nota = when {
                    esHoy && medicion.personas == 0 -> "Todavía no hay sesiones medidas en este día."
                    esHoy && medicion.bloques >= bloques -> "Día completo: están medidos los $bloques bloques."
                    esHoy -> "Faltan ${bloques - medicion.bloques} bloques para completar el día."
                    else -> null
                }
            )

            Spacer(Modifier.height(Dimens.espacioChico))
            if (ind != null) {
                val conv = ind.conversionBruta
                FilaKpis {
                    KpiMediano(
                        etiqueta = "Conversión",
                        valor = conv?.let { "${IndicadoresDia.formato(it)} %" } ?: "—",
                        detalle = if (ind.ingresos > 0) "${IndicadoresDia.formatoMiles(ind.transacciones)} de ${IndicadoresDia.formatoMiles(ind.ingresos)}" else null,
                        modifier = Modifier.weight(1f)
                    )
                    KpiMediano(
                        etiqueta = "Transacciones",
                        valor = IndicadoresDia.formatoMiles(ind.transacciones),
                        detalle = "ventas " + alcance,
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                TarjetaSeccion {
                    Text(
                        text = "La tienda todavía no cargó " + (if (esHoy) "los ingresos ni las transacciones de hoy" else "los ingresos ni las transacciones de este período"),
                        style = MaterialTheme.typography.bodySmall,
                        color = Tinta
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Sin esos dos datos no hay conversión que calcular, y no se estima.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Apagado
                    )
                }
            }

            Spacer(Modifier.height(Dimens.espacioMini))
            TextButton(onClick = { detalleAbierto = !detalleAbierto }) {
                Text(
                    text = if (detalleAbierto) "Ocultar el detalle" else "Ver el detalle",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Marino
                )
                Icon(
                    imageVector = if (detalleAbierto) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null,
                    tint = Marino
                )
            }

            if (detalleAbierto) {
                if (ind != null) {
                    Seccion("Cobertura de la medición")
                    TarjetaSeccion {
                        val total = ind.bloquesMedidos + ind.bloquesEstimados + ind.bloquesSinInformacion
                        val pct = if (total > 0) 100f * ind.bloquesMedidos / total else 0f
                        BarraPorcentaje(
                            etiqueta = "Bloques medidos",
                            valor = pct,
                            textoValor = "${ind.bloquesMedidos} de $total"
                        )
                        Spacer(Modifier.height(Dimens.espacioMini))
                        Nota(
                            if (ind.bloquesEstimados > 0)
                                "Hay ${ind.bloquesEstimados} bloques con estimación; los estimados no cuentan como medidos."
                            else if (ind.bloquesSinInformacion > 0)
                                "Quedan ${ind.bloquesSinInformacion} bloques sin información: el tránsito es parcial."
                            else
                                "Los $total bloques $alcance están medidos."
                        )
                    }
                }

                if (porAcceso.isNotEmpty()) {
                    Seccion("Ingresos por acceso")
                    TarjetaSeccion {
                        porAcceso.forEach { acc ->
                            BarraPorcentaje(
                                etiqueta = acc.acceso,
                                valor = (acc.ingresos.toFloat() / maxIngresosAcceso) * 100f,
                                textoValor = IndicadoresDia.formatoMiles(acc.ingresos)
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                if (ind != null) {
                    Seccion("Conversión sin tránsito")
                    TarjetaSeccion {
                        val convST = ind.conversionSinTransito
                        if (convST != null) {
                            Text(
                                text = "${IndicadoresDia.formato(convST)} %",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Tinta
                            )
                            Spacer(Modifier.height(2.dp))
                            Nota("Descontando el flujo continuo de personas que solo cruzan la tienda.")
                        } else {
                            Text(
                                text = "Sin información",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Tinta
                            )
                            Spacer(Modifier.height(2.dp))
                            Nota(

                                "Para calcularla tienen que estar medidos todos los bloques de los cuatro " +
                                    "accesos, y faltan ${ind.bloquesSinInformacion}. Estimarla con datos " +
                                    "parciales daría un número que no es el $alcance."
                            )
                        }
                    }
                }

                val sesiones = state.sesionesHoy
                if (esHoy) {
                    Seccion("Tus sesiones de hoy")
                    TarjetaSeccion {
                        if (sesiones.isEmpty()) {
                            Nota("No hay sesiones programadas para hoy.")
                        } else {
                            val filas = listOf(
                                EstadoSesion.VALIDADA to "Validadas",
                                EstadoSesion.FINALIZADA to "Esperando validación",
                                EstadoSesion.EN_CURSO to "En curso",
                                EstadoSesion.PROGRAMADA to "Programadas",
                                EstadoSesion.RECHAZADA to "Rechazadas"
                            )
                            filas.forEachIndexed { i, (estado, etiqueta) ->
                                val cuantas = sesiones.count { it.estado == estado }
                                if (cuantas > 0 || estado == EstadoSesion.VALIDADA) {
                                    if (i > 0) Spacer(Modifier.height(6.dp))
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(
                                            text = etiqueta,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Apagado
                                        )
                                        Text(
                                            text = cuantas.toString(),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Tinta
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

private fun fechaCorta(iso: String): String {
    val partes = iso.split("-")
    return if (partes.size == 3) partes[2] + "-" + partes[1] + "-" + partes[0] else iso
}

@Composable
private fun numeroAnimado(valor: Int): String {
    val animado by animateIntAsState(
        targetValue = valor,
        animationSpec = tween(durationMillis = 900),
        label = "numero"
    )
    return IndicadoresDia.formatoMiles(animado)
}
