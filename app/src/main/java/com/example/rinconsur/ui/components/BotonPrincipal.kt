package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.rinconsur.ui.theme.Blanco
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.GrisDeshabilitado
import com.example.rinconsur.ui.theme.RinconSurTheme

// =====================================================================
//  BotonPrincipal.kt  —  COMPONENTE REUTILIZABLE
//  ---------------------------------------------------------------------
//  Un componente es una función @Composable que:
//   1) recibe DATOS por parámetro (texto, habilitado)
//   2) recibe EVENTOS por parámetro (onClick: () -> Unit)
//   3) NO conoce el ViewModel: solo dibuja.
//
//  El parámetro "modifier" (valor por defecto Modifier) permite que la
//  pantalla decida el ancho: fillMaxWidth(), weight(1f), etc.
// =====================================================================

/** Botón de acción principal: relleno burdeo, esquinas redondeadas. */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(Dimens.alturaBotonAlto),
        enabled = habilitado,
        shape = RoundedCornerShape(Dimens.radioBoton),
        colors = ButtonDefaults.buttonColors(
            containerColor = Burdeo,
            contentColor = Blanco,
            disabledContainerColor = GrisDeshabilitado,
            disabledContentColor = Blanco
        )
    ) {
        Text(text = texto)
    }
}

// @Preview: ver el componente en Android Studio SIN ejecutar la app (vista Split).
@Preview(showBackground = true)
@Composable
private fun BotonPrincipalPreview() {
    RinconSurTheme {
        Column(Modifier.padding(Dimens.espacioMedio)) {
            BotonPrincipal(texto = "Guardar", onClick = {}, modifier = Modifier.fillMaxWidth())
            BotonPrincipal(
                texto = "Deshabilitado",
                onClick = {},
                habilitado = false,
                modifier = Modifier.fillMaxWidth().padding(top = Dimens.espacioChico)
            )
        }
    }
}