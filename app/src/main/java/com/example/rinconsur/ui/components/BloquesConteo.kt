package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Tinta

// Distribución de un grupo: etiqueta, cantidad, porcentaje y barra proporcional.
@Composable
fun BloquesConteo(grupos: List<GrupoConteo>) {
    if (grupos.isEmpty()) {
        Text("Sin registros.", style = MaterialTheme.typography.bodySmall, color = Tinta)
        return
    }
    Column(Modifier.fillMaxWidth()) {
        grupos.forEach { grupo ->
            Row(
                Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(grupo.etiqueta, style = MaterialTheme.typography.bodySmall, color = Tinta)
                Text(
                    grupo.cantidad.toString() + " · " + grupo.porcentajeTexto,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Tinta
                )
            }
            Box(
                Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)).background(Linea)
            ) {
                Box(
                    Modifier.fillMaxWidth(grupo.fraccion).height(6.dp)
                        .clip(RoundedCornerShape(3.dp)).background(Aqua)
                )
            }
        }
    }
}
