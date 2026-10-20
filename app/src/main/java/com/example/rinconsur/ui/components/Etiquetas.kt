package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AvisoSuave
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.BurdeoSuave
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.FondoSuave
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.InfoSuave
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.OkSuave
import com.example.rinconsur.ui.theme.RinconSurTheme

/** Etiqueta redondeada generica: fondo suave y texto del mismo color. */
@Composable
fun Etiqueta(texto: String, fondo: Color = FondoSuave, textoColor: Color = Apagado) {
    Box(
        Modifier
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(fondo)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = textoColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Etiqueta redondeada de estado. Nunca informa solo con color: siempre lleva la palabra. */
@Composable
fun EtiquetaEstado(texto: String, tipo: String = "neutro") {
    val par: Pair<Color, Color> = when (tipo) {
        "ok" -> OkSuave to Ok
        "aviso" -> AvisoSuave to Aviso
        "info" -> InfoSuave to Info
        "marca" -> BurdeoSuave to Burdeo
        else -> FondoSuave to Apagado
    }
    Box(
        Modifier
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(par.first)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = texto,
            style = MaterialTheme.typography.labelSmall,
            color = par.second,
            fontWeight = FontWeight.Bold
        )
    }
}

/** Titulo de seccion en mayusculas y gris. */
@Composable
fun Seccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = Apagado,
        modifier = modifier.padding(top = Dimens.espacioMedio - 4.dp, bottom = Dimens.espacioMini + 2.dp)
    )
}

/** Aclaracion chica debajo de un bloque. */
@Composable
fun Nota(texto: String, modifier: Modifier = Modifier) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = Apagado,
        modifier = modifier.padding(top = Dimens.espacioMini + 2.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun EtiquetasPreview() {
    RinconSurTheme {
        Box(Modifier.padding(8.dp)) {
            EtiquetaEstado("Por validar", "aviso")
        }
    }
}
