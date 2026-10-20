package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.rinconsur.ui.styles.EstilosBoton
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme

/** Boton de accion secundaria: contorno, mismo alto que el principal. */
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(Dimens.alturaBoton),
        enabled = habilitado,
        shape = EstilosBoton.forma,
        colors = EstilosBoton.coloresSecundario(),
        border = EstilosBoton.bordeSecundario()
    ) {
        if (icono != null) {
            Icon(icono, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        }
        Text(text = texto)
    }
}

@Preview(showBackground = true)
@Composable
private fun BotonSecundarioPreview() {
    RinconSurTheme {
        BotonSecundario(texto = "Rechazar", onClick = {})
    }
}
