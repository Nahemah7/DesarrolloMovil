package com.example.rinconsur.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rinconsur.ui.theme.BurdeoSuave
import com.example.rinconsur.ui.theme.FondoSuave
import com.example.rinconsur.ui.theme.Tinta

/**
 * Navegacion inferior. Las opciones cambian segun el perfil que entro:
 * el observador ve sus dos pantallas, el supervisor las suyas.
 */
@Composable
fun NavInferior(
    opciones: List<String>,
    actual: String,
    onIr: (String) -> Unit
) {
    NavigationBar(
        containerColor = FondoSuave,
        tonalElevation = 0.dp,
        modifier = Modifier.height(56.dp)
    ) {
        opciones.forEach { opcion ->
            NavigationBarItem(
                selected = opcion == actual,
                onClick = { onIr(opcion) },
                icon = { Box(Modifier.size(1.dp)) },
                label = {
                    Text(
                        text = opcion,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (opcion == actual) Tinta else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                alwaysShowLabel = true
            )
        }
    }
}
