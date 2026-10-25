package com.example.rinconsur.ui.screens

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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.Filtro
import com.example.rinconsur.ui.components.NavInferior
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.ProgramarViewModel

/** "2026-09-15" se muestra como 15-09-2026. */
private fun fechaLarga(fecha: String): String {
    val p = fecha.split("-")
    return if (p.size == 3) p[2] + "-" + p[1] + "-" + p[0] else fecha
}

/**
 * Programar una sesion de conteo (supervisor de piso). Solo se puede programar si el horario dura
 * entre 30 y 60 minutos y no se cruza con otra sesion del mismo acceso o de la misma persona.
 */
@Composable
fun PantallaProgramar(onIrA: (String) -> Unit) {
    val vm: ProgramarViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Hueso,
    ) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Seccion("Acceso")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                estado.accesos.forEach { a ->
                    // Etiqueta corta (A1..A4): los nombres completos no caben los cuatro y el ultimo
                    // quedaba fuera de la pantalla.
                    Filtro(a.codigo, estado.idAcceso == a.id) { vm.onAcceso(a.id) }
                }
            }
            estado.accesos.firstOrNull { it.id == estado.idAcceso }?.let { a ->
                Nota(a.codigo + " · " + a.nombre)
            }

            Seccion("Fecha")
            TarjetaSeccion {
                Text("Hoy · " + fechaLarga(estado.fecha), style = MaterialTheme.typography.bodyMedium, color = Tinta)
                Nota("Se programa para el día de trabajo en curso.")
            }

            Seccion("Horario (30 a 60 min)")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BloqueHora("Desde", estado.horaInicio, Modifier.weight(1f)) { vm.onHoraInicio(it) }
                BloqueHora("Hasta", estado.horaFin, Modifier.weight(1f)) { vm.onHoraFin(it) }
            }
            TarjetaSeccion {
                Text(
                    estado.duracionMinutos.toString() + " min",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (estado.duracionValida) Ok else Aviso
                )
                Nota(
                    if (estado.duracionValida) "Dentro del rango que pide el caso."
                    else "Tiene que durar entre 30 y 60 minutos."
                )
            }

            Seccion("Observador")
            estado.observadores.forEach { o ->
                Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    Filtro(o.nombre, estado.idObservador == o.id) { vm.onObservador(o.id) }
                    Text("  " + o.perfil.etiqueta, style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
            }

            estado.choque?.let { choque ->
                Spacer(Modifier.height(8.dp))
                TarjetaSeccion {
                    Text("Se cruza", style = MaterialTheme.typography.labelMedium, color = Aviso)
                    Text(
                        (estado.accesos.firstOrNull { it.id == choque.idAcceso }?.nombre ?: "Ese acceso") +
                            " ya tiene una sesión de " + choque.horaInicioCorta + " a " + choque.horaFinCorta +
                            (estado.observadores.firstOrNull { it.id == choque.idObservador }?.let { " (" + it.nombre + ")" } ?: "") + ".",
                        style = MaterialTheme.typography.bodySmall, color = Tinta
                    )
                }
            }

            estado.error?.let {
                Spacer(Modifier.height(8.dp))
                TarjetaSeccion { Text(it, style = MaterialTheme.typography.bodyMedium, color = Aviso) }
            }

            if (estado.programada.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                TarjetaSeccion {
                    Text(
                        "Sesión " + estado.codigo + " programada para " + estado.programada + ".",
                        style = MaterialTheme.typography.bodyMedium, color = Tinta, fontWeight = FontWeight.Bold
                    )
                    Nota("Le va a aparecer al observador cuando entre, con la opción de iniciar el conteo.")
                }
            }

            Spacer(Modifier.height(14.dp))
            BotonPrincipal("Programar sesión", onClick = { vm.programar() })
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Una hora con sus dos pasos, de 5 en 5 minutos. */
@Composable
private fun BloqueHora(etiqueta: String, hora: String, modifier: Modifier = Modifier, onCambiar: (Int) -> Unit) {
    Column(modifier) {
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = Apagado)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            TextButton(onClick = { onCambiar(-5) }) { Text("−5", color = Burdeo) }
            Text(hora, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tinta)
            TextButton(onClick = { onCambiar(5) }) { Text("+5", color = Burdeo) }
        }
    }
}
