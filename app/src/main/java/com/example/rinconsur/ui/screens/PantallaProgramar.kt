package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.layout.width
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Dimens
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.Filtro
import com.example.rinconsur.ui.components.MensajeFlotante
import com.example.rinconsur.ui.components.NavInferior
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.rinconsur.viewmodel.ProgramarViewModel
import kotlinx.coroutines.delay
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Tinta

private fun fechaLarga(fecha: String): String {
    val p = fecha.split("-")
    return if (p.size == 3) p[2] + "-" + p[1] + "-" + p[0] else fecha
}

@Composable
fun PantallaProgramar(
    onIrA: (String) -> Unit,
    idSesionEditar: Long = 0L,
    onVolver: () -> Unit = {}
) {
    val vm: ProgramarViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(idSesionEditar) {
        if (idSesionEditar > 0) vm.cargarParaEditar(idSesionEditar)
    }

    LaunchedEffect(estado.error) {
        if (estado.error != null) { delay(3500); vm.limpiarError() }
    }

    Scaffold(
        containerColor = Fondo,
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Seccion("Acceso")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                estado.accesos.forEach { a ->

                    Filtro(a.codigo, estado.idAcceso == a.id) { vm.onAcceso(a.id) }
                }
            }
            estado.accesos.firstOrNull { it.id == estado.idAcceso }?.let { a ->
                Nota(a.codigo + " · " + a.nombre)
            }

            Seccion("Fecha")
            TarjetaSeccion {
                Text("Hoy · " + fechaLarga(estado.fecha), style = MaterialTheme.typography.bodyMedium, color = Tinta)
                Nota("Se programa para el día de hoy.")
            }

            Seccion("Horario (30 a 60 min)")
            Row(
                Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                BloqueHora("Desde", estado.horaInicio, vm.horas, Modifier.weight(1f)) { vm.onHoraInicio(it) }
                BloqueHora("Hasta", estado.horaFin, vm.horas, Modifier.weight(1f)) { vm.onHoraFin(it) }
            }
            Spacer(Modifier.height(10.dp))
            TarjetaSeccion {
                Text(
                    estado.duracionMinutos.toString() + " min",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (estado.duracionValida) Ok else Aviso
                )
                Nota(
                    if (estado.duracionValida) "Duración dentro de lo permitido."
                    else "Tiene que durar entre 30 y 60 minutos."
                )
            }

            Seccion("Observador")
            var busqueda by remember { mutableStateOf("") }
            val visibles = estado.observadores.filter {
                busqueda.isBlank() || it.nombre.contains(busqueda, ignoreCase = true)
            }
            TarjetaSeccion {
                if (estado.observadores.size > 5) {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        singleLine = true,
                        label = { Text("Buscar colaborador") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                }
                Column(Modifier.heightIn(max = 280.dp).verticalScroll(rememberScrollState())) {
                    visibles.forEach { o ->
                        FilaObservador(o.nombre, o.cargo, estado.idObservador == o.id) { vm.onObservador(o.id) }
                    }
                    if (visibles.isEmpty()) {
                        Nota("No hay colaboradores que coincidan con la búsqueda.")
                    }
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
            BotonPrincipal(
                if (estado.editando) "Guardar cambios" else "Programar sesión",
                onClick = {
                    val guardado = vm.programar()
                    if (guardado) {
                        if (estado.editando) onVolver() else vm.limpiarError()
                    }
                }
            )
            Spacer(Modifier.height(20.dp))
        }
        MensajeFlotante(estado.error)
        }
    }
}

@Composable
private fun BloqueHora(
    etiqueta: String,
    hora: String,
    horas: List<String>,
    modifier: Modifier = Modifier,
    onElegir: (String) -> Unit
) {
    var abierto by remember { mutableStateOf(false) }
    val desplazamiento = remember { ScrollState(0) }
    val altoFilaPx = with(LocalDensity.current) { 48.dp.toPx() }
    LaunchedEffect(abierto) {
        if (abierto) {
            val indice = horas.indexOf(hora).coerceAtLeast(0)
            desplazamiento.scrollTo((indice * altoFilaPx).toInt())
        }
    }
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = Apagado,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(2.dp))
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, Linea, RoundedCornerShape(10.dp))
                    .clickable { abierto = true }
                    .padding(horizontal = 12.dp, vertical = 11.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(hora, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Tinta)
                Text("  ▾", fontSize = 12.sp, color = Apagado)
            }
            DropdownMenu(
                expanded = abierto,
                onDismissRequest = { abierto = false },
                modifier = Modifier.heightIn(max = (6 * 48 + 16).dp),
                scrollState = desplazamiento
            ) {
                horas.forEach { h ->
                    val elegida = h == hora
                    DropdownMenuItem(
                        text = {
                            Text(
                                h,
                                color = if (elegida) Marino else Tinta,
                                fontWeight = if (elegida) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = { onElegir(h); abierto = false }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaObservador(
    nombre: String,
    cargo: String,
    elegido: Boolean,
    onElegir: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .clickable(onClick = onElegir)
            .background(if (elegido) AquaSuave else Color.Transparent)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (elegido) FontWeight.Bold else FontWeight.Normal,
                color = Tinta
            )
            Text(cargo, style = MaterialTheme.typography.bodySmall, color = Apagado)
        }
        Box(
            Modifier
                .height(18.dp)
                .width(18.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(if (elegido) Aqua else Linea)
        )
    }
}
