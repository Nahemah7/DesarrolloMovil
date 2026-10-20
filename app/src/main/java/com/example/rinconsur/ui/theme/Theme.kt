package com.example.rinconsur.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EsquemaClaro = lightColorScheme(
    primary = Burdeo,
    onPrimary = Color.White,
    primaryContainer = BurdeoSuave,
    onPrimaryContainer = Burdeo,
    secondary = Terracota,
    onSecondary = Color.White,
    secondaryContainer = TerracotaSuave,
    onSecondaryContainer = Terracota,
    background = FondoSuave,
    onBackground = Tinta,
    surface = Color.White,
    onSurface = Tinta,
    surfaceVariant = FondoSuave,
    onSurfaceVariant = Apagado,
    outline = Linea,
    error = Aviso,
    onError = Color.White
)

/** Tema de la app. Un solo esquema claro: se usa de pie y con luz de tienda. */
@Composable
fun RinconSurTheme(contenido: @Composable () -> Unit) {
    MaterialTheme(colorScheme = EsquemaClaro, typography = TipografiaRinconSur, content = contenido)
}
