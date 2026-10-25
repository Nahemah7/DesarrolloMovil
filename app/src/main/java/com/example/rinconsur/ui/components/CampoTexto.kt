package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.example.rinconsur.ui.styles.EstilosCampo
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.RinconSurTheme

/**
 * Campo de texto con etiqueta y mensaje de error debajo.
 *
 * [etiquetaArriba] pone la etiqueta encima de la caja en vez de flotando sobre su borde. Dentro de un
 * dialogo la etiqueta flotante queda encima del texto escrito (se manchan las dos lineas), asi que ahi
 * se usa la etiqueta arriba.
 */
@Composable
fun CampoTexto(
    valor: String,
    onValorCambia: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true,
    lineas: Int = 1,
    marcador: String? = null,
    icono: ImageVector? = null,
    maximo: Int = 0,
    etiquetaArriba: Boolean = false
) {
    if (etiquetaArriba) {
        Column(modifier.fillMaxWidth()) {
            Text(
                text = etiqueta,
                style = MaterialTheme.typography.labelSmall,
                color = if (error != null) MaterialTheme.colorScheme.error else Apagado
            )
            Spacer(Modifier.height(Dimens.espacioMini))
            CampoTextoBase(
                valor = valor,
                onValorCambia = onValorCambia,
                error = error,
                tipoTeclado = tipoTeclado,
                habilitado = habilitado,
                lineas = lineas,
                marcador = marcador,
                icono = icono,
                maximo = maximo
            )
        }
        return
    }

    CampoTextoBase(
        valor = valor,
        onValorCambia = onValorCambia,
        etiqueta = etiqueta,
        error = error,
        tipoTeclado = tipoTeclado,
        habilitado = habilitado,
        lineas = lineas,
        marcador = marcador,
        icono = icono,
        maximo = maximo,
        modifier = modifier
    )
}

/** El campo en si: con etiqueta flotante, o sin ella cuando la etiqueta va arriba. */
@Composable
private fun CampoTextoBase(
    valor: String,
    onValorCambia: (String) -> Unit,
    error: String?,
    tipoTeclado: KeyboardType,
    habilitado: Boolean,
    lineas: Int,
    marcador: String?,
    icono: ImageVector?,
    maximo: Int,
    etiqueta: String = "",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorCambia,
        modifier = modifier.fillMaxWidth(),
        label = if (etiqueta.isEmpty()) null else ({ Text(etiqueta) }),
        placeholder = { if (marcador != null) Text(marcador) },
        leadingIcon = {
            if (icono != null) {
                Icon(icono, contentDescription = null, modifier = Modifier.size(Dimens.tamanoIcono))
            }
        },
        isError = error != null,
        supportingText = {
            when {
                error != null -> Text(error, color = MaterialTheme.colorScheme.error)
                maximo > 0 -> Text(valor.length.toString() + "/" + maximo)
            }
        },
        singleLine = lineas == 1,
        minLines = lineas,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(keyboardType = tipoTeclado, imeAction = ImeAction.Next),
        shape = EstilosCampo.forma,
        colors = EstilosCampo.colores()
    )
}

@Preview(showBackground = true)
@Composable
private fun CampoTextoPreview() {
    RinconSurTheme {
        CampoTexto(
            valor = "19.876.345-4",
            onValorCambia = {},
            etiqueta = "RUT",
            error = null
        )
    }
}
