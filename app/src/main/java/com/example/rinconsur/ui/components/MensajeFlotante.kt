package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Tinta

@Composable
fun BoxScope.MensajeFlotante(texto: String?) {
    texto ?: return
    Box(
        Modifier
            .align(Alignment.Center)
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Tinta.copy(alpha = 0.95f))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Text(
            texto,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
    }
}
