package com.example.rinconsur.ui.screens

import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.ui.components.BarraPorcentaje
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.IndicadoresViewModel

/**
 * Indicadores del dia, en tres bloques y en este orden: lo que ya medimos, lo que la tienda
 * registro y lo que todavia no se puede calcular.
 *
 * Regla: nada se promedia ni se rellena. Si falta medicion se dice "sin informacion" y se dice
 * cuantos bloques faltan. La conversion sin transito no se muestra hasta que el dia esta medido
 * completo: estimarla con datos parciales da un numero que no es el del dia.
 */
@Composable
fun PantallaIndicadores(onIrA: (String) -> Unit) {
    val vm: IndicadoresViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    // Al volver a la pantalla se relee: los indicadores cambian cuando se valida una sesion.
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

    val maxIngresosAcceso = porAcceso.maxOfOrNull { it.ingresos }?.takeIf { it > 0 } ?: 1

    // La barra la dibuja el contenedor: esta pantalla no arma su propio encabezado ni un menu
    // inferior, que era lo que la dejaba con dos cabeceras y con los iconos duplicados abajo.
    Scaffold(containerColor = Hueso) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (ind == null) {
                Nota(fechaCorta(state.fecha) + " · hoy")

                // Lo unico que hoy se puede mostrar: lo que llevamos medido.
                val sesiones = state.sesionesHoy
                val validadas = sesiones.count { it.estado == EstadoSesion.VALIDADA }
                val programadas = sesiones.count { it.estado == EstadoSesion.PROGRAMADA }
                val enCurso = sesiones.count { it.estado == EstadoSesion.EN_CURSO }
                val rechazadas = sesiones.count { it.estado == EstadoSesion.RECHAZADA }

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

                Seccion("Indicadores del día")
                TarjetaSeccion {
                    Text(
                        "Sin información",
                        style = MaterialTheme.typography.titleSmall,
                        color = Tinta
                    )
                    Spacer(Modifier.height(4.dp))
                    Nota(
                        "La tienda todavía no cargó los ingresos y las transacciones de hoy, y sin esos dos " +
                            "datos no hay conversión ni tránsito que calcular: no se estiman."
                    )
                }

                Nota(
                    "Cuando el conteo de la tienda esté cargado, esta pantalla muestra la conversión, " +
                        "el tránsito por acceso y el avance de la medición."
                )
            } else {
                Nota(ind.fecha + (ind.tipoDia?.let { " · " + it } ?: "") + " · datos de la tienda")

                // ---- 1) Lo que llevamos medido: el avance, con su barra animada ----
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
                            "Quedan ${ind.bloquesSinInformacion} bloque(s) sin información. Mientras falten, " +
                                "el tránsito es parcial y la conversión sin tránsito no se calcula."
                        else
                            "Los $totalBloques bloques del día están medidos: los indicadores están completos."
                    )
                }

                // ---- 2) Lo que la tienda registró: llega completo, no depende de la medición ----
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

                // ---- 3) El tránsito: solo lo medido, y dicho como parcial ----
                Seccion("Tránsito medido")
                TarjetaSeccion {
                    if (ind.bloquesMedidos == 0) {
                        Nota("Sin bloques medidos en este día: no hay tránsito que mostrar todavía.")
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

                // ---- 4) Lo que no se puede calcular todavía, dicho tal cual ----
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
                            "Para calcularla hay que descontar el tránsito de los $totalBloques bloques del día " +
                                "y faltan ${ind.bloquesSinInformacion} por medir. Estimarla con datos parciales " +
                                "daría un número que no es el del día."
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Nota("Las cifras salen de las vistas SQL del consolidado; la app no recalcula ni promedia nada.")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}

/** 2026-10-26 -> 26-10-2026, como se lee en el resto de la app. */
private fun fechaCorta(iso: String): String {
    val partes = iso.split("-")
    return if (partes.size == 3) partes[2] + "-" + partes[1] + "-" + partes[0] else iso
}

/** El número sube hasta su valor al abrir la pantalla: así se ve que el dato se está leyendo. */
@Composable
private fun numeroAnimado(valor: Int): String {
    val animado by animateIntAsState(
        targetValue = valor,
        animationSpec = tween(durationMillis = 900),
        label = "numero"
    )
    return IndicadoresDia.formatoMiles(animado)
}
