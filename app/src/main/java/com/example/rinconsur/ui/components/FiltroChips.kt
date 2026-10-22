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
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.BurdeoSuave
import com.example.rinconsur.ui.theme.RinconSurTheme

/**
 * Un filtro de una sola opcion (acceso, tipo de dia, periodo).
 * El elegido no se distingue solo por color: lleva marca y queda en negrita (RNF-07).
 */
@Composable
fun Filtro(texto: String, activo: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = activo,
        onClick = onClick,
        label = {
            Text(
                texto,
                // El elegido crece: la seleccion se percibe por tamano y peso, no solo por color (RNF-07)
                style = if (activo) MaterialTheme.typography.titleSmall else MaterialTheme.typography.labelMedium,
                fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal
            )
        },
        leadingIcon = null,
        shape = RoundedCornerShape(50),
        // La caja del elegido tambien crece: si solo crece el texto, el ojo no lo registra
        modifier = Modifier.height(if (activo) 38.dp else 32.dp),
        colors = FilterChipDefaults.filterChipColors(
            // Sobre el fondo hueso el chip sin elegir necesita superficie: si no, queda plano
            containerColor = Color.White,
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
        verticalAlignment = Alignment.CenterVertically,
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
