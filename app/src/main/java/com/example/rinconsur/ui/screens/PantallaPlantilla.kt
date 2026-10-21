package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.rinconsur.ui.components.*
import com.example.rinconsur.ui.theme.*

@Composable
fun PantallaPlantilla(onVolver: () -> Unit) {
    var clasif by remember { mutableStateOf("Tránsito") }
    var apariencia by remember { mutableStateOf("Adulto") }
    var compania by remember { mutableStateOf("Solo") }
    var compras by remember { mutableStateOf("Sí") }
    var destino by remember { mutableStateOf("A1") }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Column {
                TextButton(onClick = onVolver) { Text("‹ Volver", color = Burdeo, style = MaterialTheme.typography.labelSmall) }
                BarraApp("Plantilla de entrada", "A4 · Tamara R. · en curso", "Hora punta", Aviso)
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Nota("Configura una vez y registra con un toque. Cada etiqueta se cambia por separado, así sirve para tránsito o para visita.")
            Seccion("Clasificación")
            FilaFiltros(listOf("Tránsito", "Visita", "No determinado"), clasif) { clasif = it }
            Seccion("RangoEtario")
            FilaFiltros(listOf("Niño/a", "Joven", "Adulto joven", "Adulto", "Tercera edad", "No determinado"), apariencia) { apariencia = it }
            Seccion("Compañía")
            FilaFiltros(listOf("Solo", "Pareja", "Con niños", "Grupo"), compania) { compania = it }
            Seccion("¿Compra en otra tienda?")
            FilaFiltros(listOf("Sí", "No"), compras) { compras = it }
            Seccion("Se dirige a (solo si es tránsito)")
            FilaFiltros(listOf("A1", "A2", "A3", "A4"), destino) { destino = it }
            Spacer(Modifier.height(14.dp))
            BotonPrincipal("Aplicar la plantilla al registro", onClick = { onVolver() })
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
internal fun FilaFiltros(opciones: List<String>, elegido: String, onElegir: (String) -> Unit) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        opciones.forEach { op -> Filtro(op, op == elegido) { onElegir(op) } }
    }
}
