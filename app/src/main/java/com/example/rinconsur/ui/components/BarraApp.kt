package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.RinconSurTheme

/** Cabecera de las pantallas: titulo, quien esta adentro y su etiqueta de perfil. */
@Composable
fun BarraApp(
    titulo: String,
    subtitulo: String,
    etiqueta: String? = null,
    colorEtiqueta: Color = Burdeo,
    /** Accion opcional a la derecha de la cabecera. Va arriba para no quedar bajo los botones del sistema. */
    accion: (@Composable () -> Unit)? = null
) {
    Row(
        Modifier.fillMaxWidth().statusBarsPadding().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleMedium)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = Apagado)
        }
        if (etiqueta != null) {
            Etiqueta(texto = etiqueta, fondo = colorEtiqueta.copy(alpha = 0.12f), textoColor = colorEtiqueta)
        }
        accion?.invoke()
    }
}

@Preview(showBackground = true)
@Composable
private fun BarraAppPreview() {
    RinconSurTheme {
        BarraApp(titulo = "Registro", subtitulo = "A4 · 13:05 - 13:50 · Tamara R.", etiqueta = "En curso")
    }
}
