package com.example.rinconsur.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.MarinoClaro

// Fondo del acceso: marino, una banda diagonal mas clara con su filo de aqua
// y rayas del mismo tono. El contenido va encima, centrado.
@Composable
fun FondoAcceso(modifier: Modifier = Modifier, contenido: @Composable BoxScope.() -> Unit) {
    Box(modifier) {
        Canvas(Modifier.fillMaxSize()) {
            val ancho = size.width
            val alto = size.height

            drawRect(Marino)

            val banda = Path().apply {
                moveTo(ancho * 0.58f, 0f)
                lineTo(ancho, 0f)
                lineTo(ancho, alto)
                lineTo(ancho * 0.30f, alto)
                close()
            }
            drawPath(banda, MarinoClaro)

            val filo = Path().apply {
                moveTo(ancho * 0.535f, 0f)
                lineTo(ancho * 0.58f, 0f)
                lineTo(ancho * 0.30f, alto)
                lineTo(ancho * 0.255f, alto)
                close()
            }
            drawPath(filo, Aqua.copy(alpha = 0.85f))

            for (i in 0..7) {
                val y = alto * (0.06f + i * 0.13f)
                drawLine(
                    color = MarinoClaro.copy(alpha = 0.50f),
                    start = Offset(0f, y),
                    end = Offset(ancho * 0.20f, y - alto * 0.09f),
                    strokeWidth = 2f
                )
            }
        }
        contenido()
    }
}
