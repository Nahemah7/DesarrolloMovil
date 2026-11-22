package com.example.rinconsur.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.RinconSurTheme

@Composable
fun MarcaRinconSur(
    modifier: Modifier = Modifier,
    tamano: Dp = 40.dp,
    color: Color = Marino,
    acento: Color = Aqua
) {
    Canvas(modifier.size(tamano)) {
        val ancho = size.width
        val alto = size.height
        val grosor = ancho * 0.09f

        drawArc(
            color = color,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(ancho * 0.18f, alto * 0.12f),
            size = Size(ancho * 0.64f, alto * 0.64f),
            style = Stroke(width = grosor, cap = StrokeCap.Round)
        )
        drawLine(color, Offset(ancho * 0.18f, alto * 0.44f), Offset(ancho * 0.18f, alto * 0.84f), grosor, StrokeCap.Round)
        drawLine(color, Offset(ancho * 0.82f, alto * 0.44f), Offset(ancho * 0.82f, alto * 0.84f), grosor, StrokeCap.Round)
        drawLine(color, Offset(ancho * 0.06f, alto * 0.90f), Offset(ancho * 0.94f, alto * 0.90f), grosor * 0.85f, StrokeCap.Round)
        drawCircle(acento, radius = ancho * 0.085f, center = Offset(ancho * 0.50f, alto * 0.58f))
    }
}

@Preview(showBackground = true)
@Composable
private fun MarcaRinconSurPreview() {
    RinconSurTheme { MarcaRinconSur(tamano = 72.dp) }
}
