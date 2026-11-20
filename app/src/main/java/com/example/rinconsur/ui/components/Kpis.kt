package com.example.rinconsur.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Superficie
import com.example.rinconsur.ui.theme.Tinta

// El número que manda en la pantalla. Va solo, grande, y con el avance debajo como
// contexto: quién lo lee no tiene que interpretar nada.
@Composable
fun KpiPrincipal(
    valor: String,
    etiqueta: String,
    contexto: String? = null,
    avance: Float? = null,
    nota: String? = null
) {
    TarjetaSeccion {
        Text(
            text = etiqueta.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Apagado
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 44.sp,
            color = Tinta
        )
        if (contexto != null) {
            Text(
                text = contexto,
                style = MaterialTheme.typography.bodySmall,
                color = Apagado
            )
        }
        if (avance != null) {
            val animado by animateFloatAsState(
                targetValue = avance.coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 700),
                label = "avance"
            )
            Spacer(Modifier.height(Dimens.espacioChico))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Linea)
            ) {
                Box(
                    Modifier
                        .fillMaxWidth(animado)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (animado >= 0.999f) Aqua else Marino)
                )
            }
        }
        if (nota != null) {
            Spacer(Modifier.height(Dimens.espacioMini))
            Text(
                text = nota,
                style = MaterialTheme.typography.labelSmall,
                color = Apagado
            )
        }
    }
}

// Los KPIs de segundo orden van en fila, más chicos: se comparan entre ellos, no compiten
// con el número principal.
@Composable
fun FilaKpis(contenido: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.espacioChico)
    ) { contenido() }
}

@Composable
fun KpiMediano(
    etiqueta: String,
    valor: String,
    detalle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .clip(RoundedCornerShape(Dimens.radioTarjeta))
            .background(Superficie)
            .padding(Dimens.espacioMedio)
    ) {
        Text(
            text = etiqueta.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Apagado
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = valor,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Tinta
        )
        if (detalle != null) {
            Text(
                text = detalle,
                style = MaterialTheme.typography.labelSmall,
                color = Apagado
            )
        }
    }
}
