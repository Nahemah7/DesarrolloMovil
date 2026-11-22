package com.example.rinconsur.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.Porcion
import com.example.rinconsur.model.alturaRelativa
import com.example.rinconsur.model.porciones
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.SerieGraficos
import com.example.rinconsur.ui.theme.Tinta

@Composable
fun Donut(
    datos: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    centro: String? = null,
    centroAbajo: String? = null,
    diametro: Int = 132,
    colores: List<Color>? = null
) {
    val porciones = porciones(datos)
    if (porciones.isEmpty()) {
        Nota("Sin registros.")
        return
    }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(diametro.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val grosor = size.minDimension * 0.22f
                val inset = grosor / 2f
                val arco = Size(size.width - grosor, size.height - grosor)
                porciones.forEachIndexed { indice, porcion ->
                    if (porcion.barridoGrados <= 0f) return@forEachIndexed
                    drawArc(
                        color = colores?.getOrNull(indice) ?: SerieGraficos[indice % SerieGraficos.size],
                        startAngle = porcion.inicioGrados,
                        sweepAngle = porcion.barridoGrados,
                        useCenter = false,
                        topLeft = Offset(inset, inset),
                        size = arco,
                        style = Stroke(width = grosor, cap = StrokeCap.Butt)
                    )
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                centro?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Marino
                    )
                }
                centroAbajo?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = Apagado
                    )
                }
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            porciones.forEachIndexed { indice, porcion ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(colores?.getOrNull(indice) ?: SerieGraficos[indice % SerieGraficos.size])
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = porcion.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = Tinta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = porcion.cantidadTexto(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Tinta
                    )
                }
            }
        }
    }
}

private fun Porcion.cantidadTexto(): String =
    valor.toString() + " · " + String.format(java.util.Locale("es", "CL"), "%.1f", porcentaje).replace(".", ",") + " %"

@Composable
fun BarrasVerticales(
    datos: List<Pair<String, Int>>,
    modifier: Modifier = Modifier,
    alto: Int = 150,
    color: Color = Aqua,
    colorDestacado: Color = Marino,
    indiceDestacado: Int? = null,
    sufijo: String = "",
    textos: List<String>? = null
) {
    if (datos.isEmpty()) {
        Nota("Sin datos para graficar.")
        return
    }
    val maximo = datos.maxOf { it.second }
    Row(
        modifier.fillMaxWidth().height(alto.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        datos.forEachIndexed { indice, (etiqueta, valor) ->
            val destacado = indice == indiceDestacado
            Column(
                Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = when {
                        valor <= 0 -> "—"
                        textos != null && textos.getOrNull(indice) != null -> textos[indice]
                        else -> valor.toString() + sufijo
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = if (destacado) Tinta else Apagado,
                    fontWeight = if (destacado) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                    Box(
                        Modifier
                            .fillMaxWidth(0.72f)
                            .fillMaxHeight(alturaRelativa(valor, maximo).coerceAtLeast(if (valor > 0) 0.04f else 0f))
                            .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                            .background(if (destacado) colorDestacado else color)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = Apagado,
                    maxLines = 1,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AnilloProgreso(
    avance: Float,
    texto: String,
    subtexto: String,
    modifier: Modifier = Modifier,
    diametro: Int = 132
) {
    Box(modifier.size(diametro.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val grosor = size.minDimension * 0.16f
            val inset = grosor / 2f
            val arco = Size(size.width - grosor, size.height - grosor)
            drawArc(
                color = Linea,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arco,
                style = Stroke(width = grosor, cap = StrokeCap.Round)
            )
            if (avance > 0f) {
                drawArc(
                    color = Aqua,
                    startAngle = -90f,
                    sweepAngle = 360f * avance.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arco,
                    style = Stroke(width = grosor, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = texto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Marino
            )
            Text(text = subtexto, style = MaterialTheme.typography.labelSmall, color = Apagado)
        }
    }
}

@Composable
fun LeyendaGrafico(datos: List<Pair<String, String>>) {
    Column(Modifier.fillMaxWidth().padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        datos.forEach { (etiqueta, valor) ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(etiqueta, style = MaterialTheme.typography.labelSmall, color = Apagado)
                Text(
                    valor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Tinta
                )
            }
        }
    }
}
