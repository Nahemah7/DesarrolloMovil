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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
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
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.viewmodel.ColaboradoresViewModel
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AvisoSuave
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.OkSuave
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun PantallaColaboradores() {
    val vm: ColaboradoresViewModel = viewModel()
    val lista by vm.colaboradores.collectAsStateWithLifecycle()

    val ciclo = LocalLifecycleOwner.current
    DisposableEffect(ciclo) {
        val observador = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) vm.cargar()
        }
        ciclo.lifecycle.addObserver(observador)
        onDispose { ciclo.lifecycle.removeObserver(observador) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.espacioPantalla, vertical = Dimens.espacioChico)
    ) {
        Text(
            text = if (lista.size == 1) "1 colaborador" else lista.size.toString() + " colaboradores",
            style = MaterialTheme.typography.labelSmall,
            color = Apagado
        )
        Spacer(Modifier.height(Dimens.espacioChico))

        lista.groupBy { it.perfil }.forEach { (perfil, gente) ->
            Text(
                text = perfil.etiqueta.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Marino,
                modifier = Modifier.padding(top = Dimens.espacioMedio, bottom = Dimens.espacioMini)
            )
            gente.forEach { colaborador ->
                TarjetaSeccion(modifier = Modifier.padding(bottom = Dimens.espacioChico)) {
                    FilaColaborador(colaborador)
                }
            }
        }

        Spacer(Modifier.height(Dimens.espacioGrande))
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
