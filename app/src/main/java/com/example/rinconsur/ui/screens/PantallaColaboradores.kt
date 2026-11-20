package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.viewmodel.ColaboradoresViewModel
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AvisoSuave
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.OkSuave
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo

@Composable
fun PantallaColaboradores(onNuevaPersona: () -> Unit, onEditar: (Long) -> Unit) {
    val vm: ColaboradoresViewModel = viewModel()
    val lista by vm.colaboradores.collectAsStateWithLifecycle()
    val codigo by vm.codigo.collectAsStateWithLifecycle()
    val deEjemplo by vm.deEjemplo.collectAsStateWithLifecycle()
    var confirmandoBorrado by remember { mutableStateOf(false) }
    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.aviso.collect { host.showSnackbar(it) } }

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.cargar()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    Scaffold(containerColor = Fondo, snackbarHost = { SnackbarHost(host) }) { pad ->
    Column(
        Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(pad)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
    ) {
        BotonPrincipal(texto = "Nueva persona", onClick = onNuevaPersona)
        Spacer(Modifier.height(Dimens.espacioChico))
        Text(
            text = if (lista.size == 1) "1 colaborador" else lista.size.toString() + " colaboradores",
            style = MaterialTheme.typography.labelSmall,
            color = Apagado
        )
        Spacer(Modifier.height(Dimens.espacioChico))

        lista.groupBy { it.perfil }.forEach { (perfil, gente) ->
            Text(
                text = perfil.grupo.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Marino,
                modifier = Modifier.padding(top = Dimens.espacioMedio, bottom = Dimens.espacioMini)
            )
            gente.forEach { colaborador ->
                TarjetaSeccion(
                    modifier = Modifier
                        .padding(bottom = Dimens.espacioChico)
                        .clickable { onEditar(colaborador.id) }
                ) {
                    FilaColaborador(colaborador)
                    if (colaborador.debeCambiarClave) {
                        Row(
                            Modifier.fillMaxWidth().padding(top = Dimens.espacioMini),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sin primer acceso",
                                style = MaterialTheme.typography.labelSmall,
                                color = Aviso
                            )
                            TextButton(onClick = { vm.regenerarCodigo(colaborador) }) {
                                Text("Generar código")
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Dimens.espacioMedio))
        Text(
            text = "Datos de la aplicación",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Marino,
            modifier = Modifier.padding(top = Dimens.espacioMedio, bottom = Dimens.espacioMini)
        )
        TarjetaSeccion {
            Text(
                text = if (deEjemplo)
                    "La aplicación trae las mediciones de ejemplo del caso: el día 15-09, la semana del 08-10 y el sábado 24-10."
                else "Las mediciones de ejemplo ya se borraron: lo que se ve es lo que cargó la tienda.",
                style = MaterialTheme.typography.bodySmall,
                color = Apagado
            )
            if (deEjemplo) {
                Spacer(Modifier.height(Dimens.espacioMini))
                TextButton(onClick = { confirmandoBorrado = true }) {
                    Text(
                        text = "Empezar de cero",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Aviso
                    )
                }
            }
        }

        Spacer(Modifier.height(Dimens.espacioGrande))
    }
    }

    if (confirmandoBorrado) {
        AlertDialog(
            onDismissRequest = { confirmandoBorrado = false },
            title = { Text("Empezar de cero") },
            text = {
                Column {
                    Text(
                        "Se borran las mediciones de ejemplo: sesiones, registros, ingresos, " +
                            "transacciones, eventos y metas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Dimens.espacioChico))
                    Text(
                        "Se conservan las personas, los accesos, los bloques horarios y las preguntas " +
                            "de seguridad. Esta acción no se puede deshacer.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Apagado
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmandoBorrado = false
                    vm.empezarDeCero()
                }) {
                    Text("Borrar los datos de ejemplo", color = Aviso)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmandoBorrado = false }) { Text("Cancelar") }
            }
        )
    }

    if (codigo != null) {
        AlertDialog(
            onDismissRequest = { vm.cerrarCodigo() },
            title = { Text("Código temporal") },
            text = {
                Column {
                    Text(
                        text = codigo!!,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Marino
                    )
                    Spacer(Modifier.height(8.dp))
                    Nota("Entrégalo a la persona con su RUT. Vence en siete días.")
                }
            },
            confirmButton = { TextButton(onClick = { vm.cerrarCodigo() }) { Text("Entendido") } }
        )
    }
}

@Composable
private fun FilaColaborador(colaborador: Colaborador) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = (colaborador.nombre + " " + colaborador.apellido).trim(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(text = colaborador.cargo, style = MaterialTheme.typography.bodySmall, color = Apagado)
            Text(text = colaborador.rut, style = MaterialTheme.typography.labelSmall, color = Apagado)
        }
        Spacer(Modifier.width(Dimens.espacioChico))
        EtiquetaEstado(colaborador.activo)
    }
}

@Composable
private fun EtiquetaEstado(activo: Boolean) {
    val fondo = if (activo) OkSuave else AvisoSuave
    val texto = if (activo) Ok else Aviso
    Box(
        Modifier
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(fondo)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = if (activo) "Activo" else "Inactivo",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = texto
        )
    }
}
