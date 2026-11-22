package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Superficie
import com.example.rinconsur.ui.theme.Tinta

private const val PRIMERA = 10 * 60
private const val ULTIMA = 20 * 60
private const val PASO = 10

@Composable
fun SelectorHora(
    etiqueta: String,
    hora: String,
    onCambiar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val minutos = minutosDe(hora)
    Row(
        modifier
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(Superficie)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BotonPaso("−", habilitado = minutos > PRIMERA) { onCambiar(texto(minutos - PASO)) }
        Box(Modifier.width(76.dp), contentAlignment = Alignment.Center) {
            Text(
                text = hora,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Tinta
            )
        }
        BotonPaso("+", habilitado = minutos < ULTIMA) { onCambiar(texto(minutos + PASO)) }
    }
}

@Composable
private fun BotonPaso(simbolo: String, habilitado: Boolean, onPulsar: () -> Unit) {
    Box(
        Modifier
            .width(40.dp)
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(if (habilitado) AquaSuave else Linea)
            .clickable(enabled = habilitado, onClick = onPulsar)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(simbolo, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Tinta)
    }
}

private fun minutosDe(hora: String): Int {
    val partes = hora.split(":")
    return partes[0].toInt() * 60 + partes.getOrElse(1) { "0" }.toInt()
}

private fun texto(minutos: Int): String =
    String.format(java.util.Locale.US, "%02d:%02d", minutos / 60, minutos % 60)
