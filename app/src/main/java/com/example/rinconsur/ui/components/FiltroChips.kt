package com.example.rinconsur.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.BurdeoSuave
import com.example.rinconsur.ui.theme.RinconSurTheme

/** Un filtro de una sola opcion (acceso, tipo de dia, periodo). */
@Composable
fun Filtro(texto: String, activo: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = activo,
        onClick = onClick,
        label = { Text(texto, style = MaterialTheme.typography.labelSmall) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = BurdeoSuave,
            selectedLabelColor = Burdeo
        )
    )
}

/** Fila de filtros que se puede desplazar en horizontal. */
@Composable
fun FilaFiltros(
    opciones: List<String>,
    elegido: String,
    modifier: Modifier = Modifier,
    onElegir: (String) -> Unit
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        opciones.forEach { opcion ->
            Filtro(texto = opcion, activo = opcion == elegido, onClick = { onElegir(opcion) })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltroChipsPreview() {
    RinconSurTheme { FilaFiltros(listOf("Hoy", "Semana", "Mes"), "Hoy") {} }
}
