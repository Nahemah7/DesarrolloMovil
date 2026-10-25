package com.example.rinconsur.ui.screens

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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BarraPorcentaje
import com.example.rinconsur.ui.components.NavInferior
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.IndicadoresViewModel

@Composable
fun PantallaIndicadores(onIrA: (String) -> Unit) {
    val vm: IndicadoresViewModel = viewModel()
    val state by vm.uiState.collectAsStateWithLifecycle()

    val ind = state.indicadores
    val porAcceso = state.porAcceso

    val maxIngresosAcceso = porAcceso.maxOfOrNull { it.ingresos }?.takeIf { it > 0 } ?: 1

    Scaffold(
        containerColor = Hueso,
        topBar = {
            BarraApp(
                titulo = "Indicadores del día",
                subtitulo = ind?.let { "${it.fecha} · ${it.tipoDia}" } ?: "Sin datos de fecha"
            )
        },
        bottomBar = { NavInferior(listOf("Registrar", "Mis sesiones", "Perfil"), "Indicadores", onIrA) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (ind == null) {
                Seccion("Cargando indicadores")
                TarjetaSeccion {
                    Text(
                        "No se encontraron indicadores calculados para la fecha actual.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Tinta
                    )
                }
            } else {
                Seccion("Resumen general")
                TarjetaSeccion {
                    TarjetaEstadistica(
                        valor = IndicadoresDia.formatoMiles(ind.ingresos),
                        etiqueta = "Ingresos totales (personas registradas)"
                    )
                    Spacer(Modifier.height(8.dp))

                    TarjetaEstadistica(
                        valor = IndicadoresDia.formatoMiles(ind.transacciones),
                        etiqueta = "Transacciones (ventas realizadas)"
                    )
                    Spacer(Modifier.height(8.dp))

                    ind.conversionBruta?.let { conv ->
                        TarjetaEstadistica(
                            valor = "${IndicadoresDia.formato(conv)} %",
                            etiqueta = "Tasa de conversión bruta (Transacciones / Ingresos)"
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    ind.conversionSinTransito?.let { convST ->
                        TarjetaEstadistica(
                            valor = "${IndicadoresDia.formato(convST)} %",
                            etiqueta = "Conversión sin tránsito (descontando flujo continuo)"
                        )
                    }
                }

                if (porAcceso.isNotEmpty()) {
                    Seccion("Tránsito por acceso")
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

                Seccion("Cobertura de datos")
                TarjetaSeccion {
                    Text(
                        ind.coberturaTexto,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Tinta
                    )
                }

                Nota("Las cifras provienen del cálculo consolidado SQL y no requieren recálculo local.")
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}