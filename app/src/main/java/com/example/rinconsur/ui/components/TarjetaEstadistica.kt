package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.ui.theme.Tinta

/** Numero grande con su etiqueta debajo: los indicadores de las tarjetas. */
@Composable
fun TarjetaEstadistica(
    valor: String,
    etiqueta: String,
    color: Color = Tinta,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(text = valor, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = color)
        Text(
            text = etiqueta,
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = Apagado
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TarjetaEstadisticaPreview() {
    RinconSurTheme { TarjetaEstadistica(valor = "118", etiqueta = "registros") }
}
