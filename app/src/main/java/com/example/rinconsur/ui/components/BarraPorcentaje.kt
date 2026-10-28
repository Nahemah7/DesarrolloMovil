package com.example.rinconsur.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.FondoSuave
import com.example.rinconsur.ui.theme.RinconSurTheme

@Composable
fun BarraPorcentaje(
    etiqueta: String,
    valor: Float,
    textoValor: String,
    color: Color = Burdeo,
    atenuada: Boolean = false
) {
    val relleno = remember { Animatable(0f) }
    LaunchedEffect(valor) {
        delay(180)
        relleno.animateTo(
            targetValue = (valor / 100f).coerceIn(0.02f, 0.84f),
            animationSpec = tween(
                durationMillis = 1300,
                easing = CubicBezierEasing(0.35f, 0f, 0.25f, 1f)
            )
        )
    }

    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(Dimens.anchoEtiquetaBarra)
        )
        Box(
            Modifier
                .weight(1f)
                .height(Dimens.altoBarra)
                .clip(RoundedCornerShape(3.dp))
                .background(FondoSuave)
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = relleno.value)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (atenuada) color.copy(alpha = 0.35f) else color)
            )
            Text(
                text = textoValor,
                style = MaterialTheme.typography.labelSmall,
                color = Apagado,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BarraPorcentajePreview() {
    RinconSurTheme {
        BarraPorcentaje(etiqueta = "A4 Conexión", valor = 41f, textoValor = "41 %")
    }
}
