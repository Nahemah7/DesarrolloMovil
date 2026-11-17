package com.example.rinconsur.ui.theme

import androidx.compose.ui.graphics.Color

// Paleta Marino & Aqua. Contrastes verificados en DISENO_LOGIN.md:
// blanco sobre marino 14,57 (AAA) · aqua sobre marino 5,68 (AA) · tinta sobre aqua suave 14,28 (AAA).
// El aqua NO admite texto blanco encima (2,57): sobre aqua el texto va en marino.
val Marino = Color(0xFF0E2A47)
val MarinoClaro = Color(0xFF264668)
val Aqua = Color(0xFF12B5A6)
val AquaSuave = Color(0xFFDFF3F0)
val AquaTenue = Color(0xFFEAF7F5)   // tono para secciones sobre superficie clara

val Fondo = Color(0xFFF7F8F6)
val Superficie = Color(0xFFFFFFFF)
val Tinta = Color(0xFF16202B)
val Apagado = Color(0xFF5C6A78)
val Linea = Color(0xFFE5E9EC)
val GrisDeshabilitado = Color(0xFFB9C4CC)

val Ok = Color(0xFF0B7A6E)
val OkSuave = Color(0xFFDFF3F0)
val Aviso = Color(0xFF8A4500)
val AvisoSuave = Color(0xFFFFF7E8)
val Peligro = Color(0xFFB3261E)
val Info = Color(0xFF1F4E79)
val InfoSuave = Color(0xFFE8EFF6)

// Series de los gráficos de una sola medida (barras): marino y aqua de la marca.
val AquaMedio = Color(0xFF0F8C82)
val MarinoTenue = Color(0xFF6E8CA8)
val SerieGraficos = listOf(Marino, Aqua, MarinoClaro, AquaMedio, MarinoTenue)

// Para repartos: las porciones tienen que distinguirse entre sí de un vistazo, así que se suman
// dos tonos de otra familia (mostaza y ciruela) en vez de tres variantes del mismo azul.
val Mostaza = Color(0xFFB7791F)
val Ciruela = Color(0xFF6B2D5C)
val SerieCategorica = listOf(Marino, Aqua, Mostaza, Ciruela, MarinoClaro)
val ColoresCompras = listOf(Aqua, MarinoTenue)

val EtiquetaNino = Color(0xFF1F4E79)
val EtiquetaJoven = Color(0xFF0B7A6E)
val EtiquetaAdultoJoven = Color(0xFF4B3A8C)
val EtiquetaAdulto = Color(0xFF8A4500)
val EtiquetaTerceraEdad = Color(0xFF8C1340)
val EtiquetaNoDeterminado = Color(0xFF3A4653)
