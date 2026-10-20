package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Tinta

/** Fila de una tabla simple de tres columnas: concepto, cantidad y porcentaje. */
@Composable
fun FilaTabla(
    concepto: String,
    cantidad: String,
    porcentaje: String,
    encabezado: Boolean = false,
    total: Boolean = false
) {
    val estilo = if (encabezado) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall
    val peso = if (total) FontWeight.Bold else FontWeight.Normal
    val color = if (encabezado) Apagado else Tinta
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(concepto, modifier = Modifier.weight(1.6f), style = estilo, fontWeight = peso, color = color)
        Text(cantidad, modifier = Modifier.weight(1f), style = estilo, fontWeight = peso, color = color)
        Text(porcentaje, modifier = Modifier.weight(1f), style = estilo, fontWeight = peso, color = color)
    }
}
