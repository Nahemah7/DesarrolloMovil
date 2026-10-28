package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.MensajeFlotante
import com.example.rinconsur.ui.components.NavInferior
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.CierreViewModel
import kotlinx.coroutines.delay

private fun pct(v: Double?): String =
    if (v == null) "—" else String.format("%.1f", v).replace('.', ',') + " %"

@Composable
fun PantallaCierre(onEnviar: () -> Unit, onIrA: (String) -> Unit) {
    val vm: CierreViewModel = viewModel()
    val resumen by vm.resumen.collectAsStateWithLifecycle()
    val codigo by vm.codigo.collectAsStateWithLifecycle()
    val aviso by vm.aviso.collectAsStateWithLifecycle()
    val yaEnviada by vm.yaEnviada.collectAsStateWithLifecycle()

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.releer()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    LaunchedEffect(aviso) {
        if (aviso != null) { delay(3000); vm.limpiarAviso() }
    }

    Scaffold(
        containerColor = Hueso,
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            if (codigo.isEmpty()) {
                TarjetaSeccion {
                    Text("No tienes una sesión de conteo en curso.",
                        style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text("Inicia una desde Mi sesión para poder cerrarla.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                Spacer(Modifier.height(12.dp))
                BotonPrincipal("Ir a Mi sesión", onClick = { onIrA("Mis sesiones") })
            } else {
                TarjetaSeccion {
                    FilaTabla("Registros", "Cantidad", "%", encabezado = true)
                    FilaTabla("Tránsito", resumen.transito.toString(), pct(resumen.parteDelTotal(resumen.transito)))
                    FilaTabla("Visita", resumen.visita.toString(), pct(resumen.parteDelTotal(resumen.visita)))
                    FilaTabla("No determinado", resumen.noDeterminado.toString(), pct(resumen.parteDelTotal(resumen.noDeterminado)))
                    FilaTabla("Total", resumen.total.toString(), "100 %", total = true)
                }
                Spacer(Modifier.height(10.dp))
                TarjetaSeccion {
                    Text("Proporción de tránsito de la sesión", style = MaterialTheme.typography.labelMedium, color = Apagado)
                    Text(
                        resumen.transito.toString() + " ÷ (" + resumen.total + " − " + resumen.noDeterminado + ") = " + pct(resumen.pctTransito),
                        fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Burdeo
                    )
                    Text("Los anulados y los «no determinado» no entran en el denominador.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                if (yaEnviada) {
                    TarjetaSeccion {
                        Text("Esta sesión ya fue enviada a validación.",
                            style = MaterialTheme.typography.labelMedium, color = Tinta)
                        Text("No se le pueden agregar registros. Queda en la lista del supervisor por validar.",
                            style = MaterialTheme.typography.bodySmall, color = Apagado)
                    }
                } else {
                    Nota(
                        if (vm.sePuedeValidar) "La sesión se puede validar."
                        else "Faltan " + vm.faltan + " registros válidos para enviar: el mínimo es " + vm.minimo +
                            ". Si el teléfono está sin señal, la sesión queda por enviar y no se pierde."
                    )
                    Spacer(Modifier.height(14.dp))
                    BotonPrincipal("Enviar a validación", onClick = { if (vm.cerrar()) onEnviar() })
                }
                Spacer(Modifier.height(20.dp))
            }
        }
        MensajeFlotante(aviso)
        }
    }
}

@Composable
private fun FilaTabla(a: String, b: String, c: String, encabezado: Boolean = false, total: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        val estilo = if (encabezado) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
        val peso = if (total) FontWeight.Bold else FontWeight.Normal
        Text(a, modifier = Modifier.weight(1.6f), style = estilo, fontWeight = peso, color = if (encabezado) Apagado else Tinta)
        Text(b, modifier = Modifier.weight(1f), style = estilo, fontWeight = peso, color = if (encabezado) Apagado else Tinta)
        Text(c, modifier = Modifier.weight(1f), style = estilo, fontWeight = peso, color = if (encabezado) Apagado else Tinta)
    }
}
