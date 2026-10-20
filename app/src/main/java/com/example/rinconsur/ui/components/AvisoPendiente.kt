package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Aviso

/**
 * Marca lo que todavia no esta implementado, en pantalla y sin disimulo.
 * Se va borrando pantalla por pantalla.
 */
@Composable
fun AvisoPendiente(que: String, modifier: Modifier = Modifier) {
    TarjetaSeccion(modifier = modifier) {
        Column {
            Text(
                text = "Pendiente de implementar",
                style = MaterialTheme.typography.labelMedium,
                color = Aviso,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = que,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
