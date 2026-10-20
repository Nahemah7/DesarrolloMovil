package com.example.rinconsur.ui.styles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Blanco
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.GrisDeshabilitado

/** Forma y colores de los botones, en un solo lugar. */
object EstilosBoton {
    val forma: Shape = RoundedCornerShape(Dimens.radioBoton)
    val relleno = PaddingValues(horizontal = 18.dp, vertical = 10.dp)

    @Composable
    fun coloresPrincipal(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = Burdeo,
        contentColor = Blanco,
        disabledContainerColor = GrisDeshabilitado,
        disabledContentColor = Blanco
    )

    @Composable
    fun coloresSecundario(): ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun bordeSecundario(): BorderStroke =
        BorderStroke(Dimens.bordeBoton, MaterialTheme.colorScheme.outline)

    @Composable
    fun elevacion() = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
}

fun Modifier.estiloAltoBoton(): Modifier = this.height(Dimens.alturaBoton)
