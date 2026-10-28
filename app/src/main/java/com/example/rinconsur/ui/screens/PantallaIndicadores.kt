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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.TipoPeriodo
import com.example.rinconsur.ui.components.BarraPorcentaje
import com.example.rinconsur.ui.components.FilaFiltros
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Hueso
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
    val porAcceso = state.porAcceso
    val esHoy = state.periodo == TipoPeriodo.HOY
    val cabecera = if (esHoy) fechaCorta(state.fecha) + " · hoy"
        else {
            val dias = ind?.dias ?: 0
            state.periodo.etiqueta + " · " + dias + (if (dias == 1) " día" else " días") +
                (ind?.let { " (" + fechaCorta(it.desde) + " a " + fechaCorta(it.hasta) + ")" } ?: "")
        }
    val alcance = if (esHoy) "del día" else "del período"

    val maxIngresosAcceso = porAcceso.maxOfOrNull { it.ingresos }?.takeIf { it > 0 } ?: 1

    Scaffold(containerColor = Hueso) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
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
            Spacer(Modifier.height(10.dp))

            if (ind == null) {
                Nota(cabecera)

                val sesiones = state.sesionesHoy
                val validadas = sesiones.count { it.estado == EstadoSesion.VALIDADA }
                val programadas = sesiones.count { it.estado == EstadoSesion.PROGRAMADA }
                val enCurso = sesiones.count { it.estado == EstadoSesion.EN_CURSO }
                val rechazadas = sesiones.count { it.estado == EstadoSesion.RECHAZADA }

                if (esHoy) {
                Seccion("Tu medición de hoy")
                TarjetaSeccion {
                    if (sesiones.isEmpty()) {
                        Nota("Todavía no hay sesiones programadas para hoy.")
                    } else {
                        TarjetaEstadistica(valor = numeroAnimado(validadas), etiqueta = "Sesiones validadas")
                        Spacer(Modifier.height(8.dp))
                        TarjetaEstadistica(valor = numeroAnimado(programadas), etiqueta = "Programadas, por medir")
                        Spacer(Modifier.height(8.dp))
                        TarjetaEstadistica(valor = numeroAnimado(enCurso), etiqueta = "En curso")
                        if (rechazadas > 0) {
                            Spacer(Modifier.height(8.dp))
                            TarjetaEstadistica(valor = numeroAnimado(rechazadas), etiqueta = "Rechazadas")
                        }
                    }
                }
                }

                Seccion(if (esHoy) "Indicadores del día" else "Indicadores del período")
                TarjetaSeccion {
                    Text(
                        "Sin información",
                        style = MaterialTheme.typography.titleSmall,
                        color = Tinta
                    )
                    Spacer(Modifier.height(4.dp))
                    Nota(
                        if (esHoy)
                            "La tienda todavía no cargó los ingresos y las transacciones de hoy, y sin esos dos " +
                                "datos no hay conversión ni tránsito que calcular: no se estiman."
                        else
                            "Ningún día de este período tiene ingresos y transacciones cargados, y sin esos dos " +
                                "datos no hay conversión ni tránsito que calcular: no se estiman."
                    )
                }

                Nota(
                    if (esHoy && state.hayDatosEnHabiles)
                        "Los días hábiles tienen cifras cargadas: están en el filtro «Hábiles»."
                    else
                        "Cuando el conteo de la tienda esté cargado, esta pantalla muestra la conversión, " +
                            "el tránsito por acceso y el avance de la medición."
                )
            } else {
                Nota(cabecera + " · datos de la tienda")

                val totalBloques = ind.bloquesMedidos + ind.bloquesEstimados + ind.bloquesSinInformacion
                val pctMedido = if (totalBloques > 0) 100f * ind.bloquesMedidos / totalBloques else 0f

                Seccion("Avance de la medición")
                TarjetaSeccion {
                    BarraPorcentaje(
                        etiqueta = "Medidos",
                        valor = pctMedido,
                        textoValor = "${ind.bloquesMedidos} de $totalBloques"
                    )
                    Spacer(Modifier.height(8.dp))
                    Nota(
                        if (ind.bloquesSinInformacion > 0)
                            (if (ind.bloquesSinInformacion == 1) "Queda 1 bloque sin información. Mientras falte, "
                            else "Quedan ${ind.bloquesSinInformacion} bloques sin información. Mientras falten, ") +
                                "el tránsito es parcial y la conversión sin tránsito no se calcula."
                        else
                            "Los $totalBloques bloques $alcance están medidos: los indicadores están completos."
                    )
                }

                Seccion("Lo que la tienda registró")
                TarjetaSeccion {
                    TarjetaEstadistica(
                        valor = numeroAnimado(ind.ingresos),
                        etiqueta = "Ingresos totales (personas registradas)"
                    )
                    Spacer(Modifier.height(8.dp))

                    TarjetaEstadistica(
                        valor = numeroAnimado(ind.transacciones),
                        etiqueta = "Transacciones (ventas realizadas)"
                    )

                    ind.conversionBruta?.let { conv ->
                        Spacer(Modifier.height(8.dp))
                        TarjetaEstadistica(
                            valor = "${IndicadoresDia.formato(conv)} %",
                            etiqueta = "Tasa de conversión bruta (Transacciones / Ingresos)"
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    Nota("Estos datos vienen del conteo de la tienda: están completos y no dependen de lo que medimos.")
                }

                Seccion("Tránsito medido")
                TarjetaSeccion {
                    if (ind.bloquesMedidos == 0) {
                        Nota("Sin bloques medidos en este " + (if (esHoy) "día" else "período") + ": no hay tránsito que mostrar todavía.")
                    } else {
                        TarjetaEstadistica(
                            valor = IndicadoresDia.formatoMiles((ind.transito ?: 0.0).toInt()),
                            etiqueta = "Personas en tránsito, en ${ind.bloquesMedidos} de $totalBloques bloques"
                        )
                        Spacer(Modifier.height(8.dp))
                        Nota("Es lo medido, no una proyección: falta cubrir los otros bloques para tener el día completo.")
                    }
                }

                if (porAcceso.isNotEmpty()) {
                    Seccion("Ingresos por acceso")
                    TarjetaSeccion {
                        porAcceso.forEach { acc ->
                            val pct = (acc.ingresos.toFloat() / maxIngresosAcceso) * 100f
                            BarraPorcentaje(
                                etiqueta = acc.acceso,
                                valor = pct,
                                textoValor = IndicadoresDia.formatoMiles(acc.ingresos)
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }

                Seccion("Conversión sin tránsito")
                TarjetaSeccion {
                    val convST = ind.conversionSinTransito
                    if (convST != null) {
                        TarjetaEstadistica(
                            valor = "${IndicadoresDia.formato(convST)} %",
                            etiqueta = "Conversión sin tránsito (descontando el flujo continuo)"
                        )
                    } else {
                        Text(
                            "Sin información",
                            style = MaterialTheme.typography.titleSmall,
                            color = Tinta
                        )
                        Spacer(Modifier.height(4.dp))
                        Nota(
                            "Para calcularla hay que descontar el tránsito de los $totalBloques bloques $alcance " +
                                "y faltan ${ind.bloquesSinInformacion} por medir. Estimarla con datos parciales " +
                                "daría un número que no es el del día."
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Nota("Las cifras se calculan con los ingresos y las transacciones que carga la tienda.")
            }

            Spacer(Modifier.height(20.dp))
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
