package com.example.rinconsur.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.rinconsur.ui.styles.EstilosBoton
import com.example.rinconsur.ui.styles.estiloAltoBoton
import com.example.rinconsur.ui.theme.RinconSurTheme

// =====================================================================
//  BotonPrincipal.kt  —  COMPONENTE REUTILIZABLE
//  ---------------------------------------------------------------------
//  Un componente es una funcion @Composable que:
//   1) recibe DATOS por parametro (texto, habilitado)
//   2) recibe EVENTOS por parametro (onClick: () -> Unit)
//   3) NO conoce el ViewModel: solo dibuja.
//
//  El parametro "modifier" (valor por defecto Modifier) permite que la
//  pantalla decida el ancho: fillMaxWidth(), weight(1f), etc.
// =====================================================================

/**
 * Boton principal de la app: ancho completo, alto fijo y una sola accion clara.
 *
 * ANIMACION 3: al mantener el dedo, el boton se achica un 3 % y vuelve con un resorte. Es
 * retroalimentacion: la persona sabe que el toque entro incluso antes de que aparezca el
 * resultado, y en esta app se usa de pie y con una mano.
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    icono: ImageVector? = null
) {
    val interaccion = remember { MutableInteractionSource() }
    val apretado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (apretado) 0.97f else 1f,
        animationSpec = spring(),
        label = "escalaBoton"
    )

    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().estiloAltoBoton().scale(escala),
        interactionSource = interaccion,
        enabled = habilitado,
        shape = EstilosBoton.forma,
        colors = EstilosBoton.coloresPrincipal(),
        elevation = EstilosBoton.elevacion(),
        contentPadding = EstilosBoton.relleno
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
private fun BotonPrincipalPreview() {
    RinconSurTheme {
        BotonPrincipal(texto = "Iniciar sesion", onClick = {})
    }
}
