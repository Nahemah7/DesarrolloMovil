package com.example.rinconsur.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.rinconsur.model.RangoEtario

fun colorDeRango(rango: RangoEtario): Color = when (rango) {
    RangoEtario.MENOR_18 -> EtiquetaNino
    RangoEtario.DE_18_A_25 -> EtiquetaJoven
    RangoEtario.DE_26_A_40 -> EtiquetaAdultoJoven
    RangoEtario.DE_41_A_60 -> EtiquetaAdulto
    RangoEtario.DE_61_O_MAS -> EtiquetaTerceraEdad
}

fun colorDeRango(etiqueta: String): Color? =
    RangoEtario.entries.firstOrNull { it.etiqueta == etiqueta }?.let { colorDeRango(it) }
