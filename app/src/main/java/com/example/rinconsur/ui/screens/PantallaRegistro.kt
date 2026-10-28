package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.Filtro
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.MensajeFlotante
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaEstadistica
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.EtiquetaAdulto
import com.example.rinconsur.ui.theme.EtiquetaAdultoJoven
import com.example.rinconsur.ui.theme.EtiquetaJoven
import com.example.rinconsur.ui.theme.EtiquetaNino
import com.example.rinconsur.ui.theme.EtiquetaTerceraEdad
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.HuesoLinea
import com.example.rinconsur.ui.theme.HuesoApagado
import androidx.compose.foundation.layout.ColumnScope
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.data.Vibracion
import com.example.rinconsur.viewmodel.RegistroViewModel
import kotlinx.coroutines.delay

private fun nombreDe(rango: RangoEtario): String = when (rango) {
    RangoEtario.MENOR_18 -> "Niño/a"
    RangoEtario.DE_18_A_25 -> "Joven"
    RangoEtario.DE_26_A_40 -> "Adulto joven"
    RangoEtario.DE_41_A_60 -> "Adulto"
    RangoEtario.DE_61_O_MAS -> "Tercera edad"
}

private fun colorDe(rango: RangoEtario): Color = when (rango) {
    RangoEtario.MENOR_18 -> EtiquetaNino
    RangoEtario.DE_18_A_25 -> EtiquetaJoven
    RangoEtario.DE_26_A_40 -> EtiquetaAdultoJoven
    RangoEtario.DE_41_A_60 -> EtiquetaAdulto
    RangoEtario.DE_61_O_MAS -> EtiquetaTerceraEdad
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BotonPlantilla(numero: Int, tiene: Boolean, alTocar: () -> Unit, alMantener: () -> Unit) {
    Box(
        Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (tiene) com.example.rinconsur.ui.theme.BurdeoSuave else Color.White)
            .border(1.dp, if (tiene) Burdeo else Linea, RoundedCornerShape(12.dp))
            .combinedClickable(onClick = alTocar, onLongClick = alMantener),
        contentAlignment = Alignment.Center
    ) {
        Text(numero.toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (tiene) Burdeo else Apagado)
    }
}

@Composable
private fun RowScope.BotonRango(nombre: String, rango: String, color: Color, elegido: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .weight(1f)
            .height(if (elegido) 62.dp else 54.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (elegido) color else Color.White)
            .border(1.dp, if (elegido) color else HuesoLinea, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.size(7.dp).clip(RoundedCornerShape(99.dp))
                    .background(if (elegido) Color.Transparent else color)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = nombre,
                color = if (elegido) Color.White else Tinta,
                fontSize = if (elegido) 17.5.sp else 14.5.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 15.sp
            )
            Text(
                text = rango,
                color = if (elegido) Color.White.copy(alpha = 0.9f) else HuesoApagado,
                fontSize = if (elegido) 12.sp else 10.5.sp,
                textAlign = TextAlign.Center,
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun Bloque(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, HuesoLinea, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        content = contenido
    )
}

@Composable
fun PantallaRegistro(onPlantilla: (Int) -> Unit, onCerrar: () -> Unit, onIrA: (String) -> Unit) {
    val vm: RegistroViewModel = viewModel()
    val contexto = LocalContext.current
    val plantillas by vm.plantillas.collectAsStateWithLifecycle()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    var aAnular by remember { mutableStateOf<Long?>(null) }


    aAnular?.let { id ->
        AlertDialog(
            onDismissRequest = { aAnular = null },
            title = { Text("¿Anular este registro?") },
            text = { Text("Deja de contarse y queda registrado quién lo anuló. No se borra.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.anular(id, "Anulado por el observador durante la sesión")
                    aAnular = null
                }) { Text("Sí, anular") }
            },
            dismissButton = { TextButton(onClick = { aAnular = null }) { Text("Cancelar") } }
        )
    }

    LaunchedEffect(Unit) { vm.recargarPlantillas() }

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.releer()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    LaunchedEffect(estado.mensaje) {
        if (estado.mensaje != null) { delay(2500); vm.limpiarMensaje() }
    }

    Scaffold(
        containerColor = Hueso,
        bottomBar = {
            Column(
                Modifier
                    .background(Color.White)
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                BotonPrincipal("Guardar", onClick = { if (vm.guardar()) Vibracion.confirmarGuardado(contexto) })
            }
        }
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            if (estado.sinSesionEnCurso) {
                TarjetaSeccion {
                    Text("No tienes una sesión de conteo en curso.",
                        style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text("Inicia una desde Mi sesión para poder registrar.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                Spacer(Modifier.height(12.dp))
                BotonPrincipal("Ir a Mi sesión", onClick = { onIrA("Mis sesiones") })
                Spacer(Modifier.height(16.dp))
            }
            Bloque {
                val pct = estado.resumen.pctTransito?.let {
                    String.format("%.1f", it).replace('.', ',') + " %"
                } ?: "—"
                Text(
                    estado.resumen.total.toString(),
                    fontSize = 34.sp, fontWeight = FontWeight.ExtraBold,
                    color = Tinta, letterSpacing = (-1).sp, lineHeight = 36.sp
                )
                Text("registros en esta sesión", style = MaterialTheme.typography.labelSmall, color = HuesoApagado)
                Spacer(Modifier.height(10.dp))
                Box(
                    Modifier.fillMaxWidth().height(9.dp)
                        .clip(RoundedCornerShape(99.dp)).background(HuesoLinea)
                ) {
                    Box(
                        Modifier.fillMaxHeight()
                            .fillMaxWidth(fraction = ((estado.resumen.pctTransito ?: 0.0) / 100.0).toFloat().coerceIn(0.02f, 1f))
                            .clip(RoundedCornerShape(99.dp)).background(Burdeo)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Proporción de tránsito", style = MaterialTheme.typography.labelSmall, color = HuesoApagado)
                    Text(pct + " · " + estado.resumen.transito + " en tránsito",
                        style = MaterialTheme.typography.labelSmall, color = Burdeo)
                }
                if (!estado.sinSesionEnCurso) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EtiquetaEstado("En curso", "ok")
                        TextButton(onClick = onCerrar) {
                            Text("Enviar sesión", color = Burdeo, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Seccion("Plantillas de registro rápido")
            Bloque {
                Text("Si entra alguien del mismo perfil, toca un número para aplicarla.",
                    style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (1..3).forEach { n ->
                        BotonPlantilla(
                            numero = n,
                            tiene = plantillas.getOrNull(n - 1) != null,
                            alTocar = { vm.aplicarPlantilla(n) },
                            alMantener = { onPlantilla(n) }
                        )
                    }
                }
                Nota("Mantén apretado un número para editar esa plantilla.")
            }

            Seccion("Clasificación")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Clasificacion.entries.forEach { c ->
                    Filtro(c.etiqueta, estado.clasificacion == c) { vm.onClasificacion(c) }
                }
            }

            Seccion("Apariencia (la edad no se pregunta)")
            RangoEtario.entries.chunked(3).forEachIndexed { fila, tramos ->
                if (fila > 0) Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tramos.forEach { r ->
                        BotonRango(nombreDe(r), r.etiqueta, colorDe(r), estado.rangoEtario == r) { vm.onRangoEtario(r) }
                    }
                }
            }
            Nota("Se estima de un vistazo y nunca se le pregunta a la persona.")

            Seccion("Compañía")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Compania.entries.forEach { c ->
                    Filtro(c.etiqueta, estado.compania == c) { vm.onCompania(c) }
                }
            }

            Seccion("Compras en otra tienda")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Filtro("Sí", estado.comprasOtraTienda) { vm.onCompras(true) }
                Filtro("No", !estado.comprasOtraTienda) { vm.onCompras(false) }
            }

            if (estado.clasificacion == Clasificacion.TRANSITO) {
                Seccion("Hacia dónde se dirige")
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    estado.accesos.forEach { a ->
                        Filtro(a.codigo, estado.destino == a.id) { vm.onDestino(a.id) }
                    }
                }
                estado.accesos.firstOrNull { it.id == estado.destino }?.let { a ->
                    Nota("Va a " + a.codigo + " · " + a.nombre)
                }
            }


            if (estado.ultimosRegistros.isNotEmpty()) {
                Seccion("Últimos registros")
                Bloque {
                    estado.ultimosRegistros.take(3).forEach { r ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                r.hora + " · " + r.clasificacion.etiqueta + " · " + r.rangoEtario.etiqueta +
                                    " · " + r.compania.etiqueta,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            if (r.anulado) {
                                EtiquetaEstado("Anulado", "aviso")
                            } else if (estado.sesionEnCurso) {
                                TextButton(onClick = { aAnular = r.id }) {
                                    Text("Anular", style = MaterialTheme.typography.labelSmall, color = Burdeo)
                                }
                            }
                        }
                    }
                    if (!estado.sesionEnCurso) {
                        Text("Solo se puede anular mientras la sesión está en curso.",
                            style = MaterialTheme.typography.bodySmall, color = com.example.rinconsur.ui.theme.Apagado)
                    }
                }
            }

            if (estado.resumen.sePuedeValidar) {
                Nota("La sesión ya se puede validar.")
            } else {
                Nota("Desde 30 registros válidos la sesión se puede validar.")
            }
            Spacer(Modifier.height(20.dp))
        }
            MensajeFlotante(estado.mensaje)
        }
    }
}
