package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AvisoSuave
import com.example.rinconsur.ui.theme.Dimens

// Se muestra mientras la aplicación tenga los datos de ejemplo del caso.
@Composable
fun EtiquetaEjemplo(modifier: Modifier = Modifier) {
    Box(
        modifier
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(AvisoSuave)
            .padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(
            text = "Datos de ejemplo",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = Aviso
        )
    }
}
