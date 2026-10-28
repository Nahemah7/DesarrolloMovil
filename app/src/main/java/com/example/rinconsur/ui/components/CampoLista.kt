package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.styles.EstilosCampo
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Blanco
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.RinconSurTheme
import com.example.rinconsur.ui.theme.Tinta

@Composable
fun CampoLista(
    etiqueta: String,
    textoElegido: String,
    opciones: List<String>,
    alElegir: (Int) -> Unit,
    modifier: Modifier = Modifier,
    marcador: String = "Seleccione una opción",
    error: String? = null,
    habilitado: Boolean = true
) {
    var abierto by remember { mutableStateOf(false) }
    val vacio = textoElegido.isEmpty()
    val valorEnPantalla = if (vacio) marcador else textoElegido
    val colorTexto = if (vacio) Apagado else Tinta
    val colorBorde = if (error != null) MaterialTheme.colorScheme.error else Linea

    Column(modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelSmall,
            color = if (error != null) MaterialTheme.colorScheme.error else Apagado
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Blanco, EstilosCampo.forma)
                    .border(1.dp, colorBorde, EstilosCampo.forma)
                    .clickable(enabled = habilitado) { abierto = true }
                    .padding(horizontal = 16.dp)
                    .semantics { contentDescription = etiqueta + ": " + valorEnPantalla },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = valorEnPantalla,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorTexto,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    Icons.Filled.ArrowDropDown,
                    contentDescription = null,
                    tint = Apagado,
                    modifier = Modifier.size(Dimens.tamanoIcono)
                )
            }

            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                opciones.forEachIndexed { indice, opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            alElegir(indice)
                            abierto = false
                        }
                    )
                }
            }
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CampoListaPreview() {
    RinconSurTheme {
        CampoLista(
            etiqueta = "Pregunta",
            textoElegido = "",
            opciones = listOf("¿Cuál es el nombre de tu primera mascota?", "¿En qué ciudad nació tu madre?"),
            alElegir = {}
        )
    }
}
