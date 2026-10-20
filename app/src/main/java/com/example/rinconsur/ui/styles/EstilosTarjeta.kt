package com.example.rinconsur.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Linea

object EstilosTarjeta {
    val forma: Shape = RoundedCornerShape(Dimens.radioTarjeta)

    @Composable
    fun colores(): CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )

    @Composable
    fun elevacion(): CardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    @Composable
    fun borde(): BorderStroke = BorderStroke(1.dp, Linea)
}

