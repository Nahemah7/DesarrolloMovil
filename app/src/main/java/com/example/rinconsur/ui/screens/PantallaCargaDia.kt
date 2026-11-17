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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.BloqueCarga
import com.example.rinconsur.model.CargaDia
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.viewmodel.CargaDiaViewModel

// Carga del día: once bloques con ingresos y boletas, y el reparto por acceso como detalle.
@Composable
fun PantallaCargaDia() {
    val vm: CargaDiaViewModel = viewModel()
    val carga by vm.carga.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.aviso.collect { host.showSnackbar(it) } }
    LaunchedEffect(Unit) { vm.cargar() }

    Scaffold(containerColor = Fondo, snackbarHost = { SnackbarHost(host) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            val dia = carga
            if (dia == null) {
                Nota("Cargando la jornada.")
                return@Column
            }

            Nota("Ingresos del local y boletas del punto de venta, bloque por bloque. El reparto por acceso es opcional: si no lo abres, el bloque queda como total del día.")
            Seccion("Bloques del día")

            dia.bloques.forEach { bloque ->
                TarjetaSeccion(modifier = Modifier.padding(bottom = Dimens.espacioChico)) {
                    FilaBloque(
                        bloque = bloque,
                        accesos = dia.accesos,
                        onIngresos = { vm.onIngresos(bloque.idBloque, it) },
                        onBoletas = { vm.onBoletas(bloque.idBloque, it) },
                        onDetalle = { idAcceso, valor -> vm.onDetalle(bloque.idBloque, idAcceso, valor) },
                        onAlternarDetalle = { vm.alternarDetalle(bloque.idBloque) }
                    )
                }
            }

            Seccion("Resumen del día")
            TarjetaSeccion {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ingresos", style = MaterialTheme.typography.bodySmall, color = Apagado)
                    Text(
                        dia.totalIngresos.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Tinta
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Boletas", style = MaterialTheme.typography.bodySmall, color = Apagado)
                    Text(
                        dia.totalBoletas.toString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Tinta
                    )
                }
                Spacer(Modifier.height(Dimens.espacioMini))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Conversión", style = MaterialTheme.typography.bodySmall, color = Apagado)
                    Text(
                        dia.conversionTexto,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Marino
                    )
                }
                Spacer(Modifier.height(Dimens.espacioMini))
                Nota(dia.bloquesConIngresos.toString() + " de 11 bloques con ingresos · " +
                    dia.bloquesConBoletas + " con boletas · " + dia.bloquesConDetalle + " con reparto por acceso")
                if (dia.bloquesAPartar > 0) {
                    Spacer(Modifier.height(Dimens.espacioMini))
                    Text(
                        text = if (dia.bloquesAPartar == 1)
                            "1 bloque tiene solo uno de los dos datos: sin ingresos y boletas no se calcula la conversión."
                        else dia.bloquesAPartar.toString() +
                            " bloques tienen solo uno de los dos datos: sin ingresos y boletas no se calcula la conversión.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Aviso
                    )
                }
                dia.errores.firstOrNull()?.let { falta ->
                    Spacer(Modifier.height(Dimens.espacioMini))
                    Text(
                        text = falta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))
            BotonPrincipal(
                texto = "Guardar el día",
                onClick = { vm.guardar() },
                habilitado = dia.puedeGuardar
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun FilaBloque(
    bloque: BloqueCarga,
    accesos: List<com.example.rinconsur.model.Acceso>,
    onIngresos: (String) -> Unit,
    onBoletas: (String) -> Unit,
    onDetalle: (Long, String) -> Unit,
    onAlternarDetalle: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = bloque.hora,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = Marino,
            modifier = Modifier.width(64.dp)
        )
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f)) {
                    CampoTexto(
                        valor = bloque.ingresos,
                        onValorCambia = onIngresos,
                        etiqueta = "Ingresos",
                        tipoTeclado = KeyboardType.Number
                    )
                }
                Spacer(Modifier.width(Dimens.espacioMini))
                Box(Modifier.weight(1f)) {
                    CampoTexto(
                        valor = bloque.boletas,
                        onValorCambia = onBoletas,
                        etiqueta = "Boletas",
                        tipoTeclado = KeyboardType.Number
                    )
                }
            }
        }
    }

    Row(
        Modifier.fillMaxWidth().padding(top = Dimens.espacioMini),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (bloque.detallado) {
            val cuadra = bloque.detalleCuadra
            Text(
                text = "Reparto: " + bloque.sumaDetalle + " de " + (bloque.ingresosNumero ?: 0),
                style = MaterialTheme.typography.labelSmall,
                color = if (cuadra) Ok else Aviso,
                fontWeight = FontWeight.SemiBold
            )
        } else {
            Text(
                text = "Total del bloque",
                style = MaterialTheme.typography.labelSmall,
                color = Apagado
            )
        }
        TextButton(onClick = onAlternarDetalle) {
            Text(if (bloque.detallado) "Quitar reparto" else "Repartir por acceso")
        }
    }

    if (bloque.detallado) {
        Spacer(Modifier.height(Dimens.espacioMini))
        accesos.forEach { acceso ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = acceso.codigo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Apagado,
                    modifier = Modifier.width(36.dp)
                )
                Box(Modifier.weight(1f)) {
                    CampoTexto(
                        valor = bloque.detalle[acceso.id] ?: "",
                        onValorCambia = { onDetalle(acceso.id, it) },
                        etiqueta = acceso.codigo,
                        tipoTeclado = KeyboardType.Number
                    )
                }
            }
            Spacer(Modifier.height(Dimens.espacioMini))
        }
        Box(
            Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)).background(Linea)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(
                        if ((bloque.ingresosNumero ?: 0) > 0)
                            (bloque.sumaDetalle.toFloat() / (bloque.ingresosNumero ?: 1)).coerceIn(0f, 1f)
                        else 0f
                    )
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (bloque.detalleCuadra) Aqua else Aviso)
            )
        }
    }
}
