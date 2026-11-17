package com.example.rinconsur.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.MarinoClaro
import com.example.rinconsur.ui.theme.Peligro
import com.example.rinconsur.ui.theme.Superficie
import com.example.rinconsur.ui.theme.Tinta

private val EsquemaClaro = lightColorScheme(
    primary = Marino,
    onPrimary = Superficie,
    primaryContainer = AquaSuave,
    onPrimaryContainer = Marino,
    secondary = Aqua,
    onSecondary = Marino,
    secondaryContainer = AquaSuave,
    onSecondaryContainer = Marino,
    tertiary = MarinoClaro,
    onTertiary = Superficie,
    background = Fondo,
    onBackground = Tinta,
    surface = Superficie,
    onSurface = Tinta,
    surfaceVariant = AquaSuave,
    onSurfaceVariant = Apagado,
    outline = Linea,
    outlineVariant = Linea,
    error = Peligro,
    onError = Superficie
)

@Composable
fun RinconSurTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaClaro, typography = TipografiaRinconSur, content = contenido)
}
