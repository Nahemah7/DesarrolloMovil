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
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.FuenteDatos
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino

// Marca discreta de origen: cuando las cifras vienen del servidor se dice, y cuando son la copia
// local también, para que nadie confunda datos sin sincronizar con datos reales.
@Composable
fun EtiquetaFuente(fuente: FuenteDatos) {
    val enLinea = fuente == FuenteDatos.EN_LINEA
    Box(
        Modifier
            .clip(RoundedCornerShape(Dimens.radioEtiqueta))
            .background(if (enLinea) AquaSuave else Linea)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = if (enLinea) "En línea" else "Local",
            style = MaterialTheme.typography.labelSmall,
            color = if (enLinea) Marino else Apagado
        )
    }
}
