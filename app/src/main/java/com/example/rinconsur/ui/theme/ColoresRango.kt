package com.example.rinconsur.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.rinconsur.model.RangoEtario

// Un color por tramo de edad, el mismo que usan los botones de la plantilla del observador.
// Vive acá para que el reporte y el registro no tengan dos copias que se separen.
fun colorDeRango(rango: RangoEtario): Color = when (rango) {
    RangoEtario.MENOR_18 -> EtiquetaNino
    RangoEtario.DE_18_A_25 -> EtiquetaJoven
    RangoEtario.DE_26_A_40 -> EtiquetaAdultoJoven
    RangoEtario.DE_41_A_60 -> EtiquetaAdulto
    RangoEtario.DE_61_O_MAS -> EtiquetaTerceraEdad
}

// Los informes traen el nombre del tramo, no el id: se traduce con la misma tabla.
fun colorDeRango(etiqueta: String): Color? =
    RangoEtario.entries.firstOrNull { it.etiqueta == etiqueta }?.let { colorDeRango(it) }
