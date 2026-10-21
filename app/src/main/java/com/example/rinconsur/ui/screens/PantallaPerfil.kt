package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.viewmodel.EntradaViewModel

/**
 * Perfil de quien esta usando el telefono. Los datos salen del colaborador que entro, no de la
 * pantalla: se leen de la base con la sesion guardada.
 */
@Composable
fun PantallaPerfil(onSalir: () -> Unit) {
    val vm: EntradaViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            BarraApp(
                titulo = estado.nombre.ifEmpty { "Mi perfil" },
                subtitulo = estado.perfil.etiqueta,
                etiqueta = "Mi perfil",
                colorEtiqueta = Info
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Seccion("Mis datos")
            TarjetaSeccion(acento = Burdeo) {
                Text(estado.nombre, style = MaterialTheme.typography.titleSmall)
                Text(estado.perfil.etiqueta, style = MaterialTheme.typography.bodyMedium)
                Text(estado.rut, style = MaterialTheme.typography.bodySmall, color = Apagado)
                Text("Un solo perfil por colaborador", style = MaterialTheme.typography.bodySmall, color = Apagado)
            }
            Seccion("Accesos de la tienda")
            TarjetaSeccion {
                estado.accesos.forEach { acceso ->
                    Text(acceso.etiquetaCorta, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(14.dp))
            BotonPrincipal("Cerrar sesion", onClick = onSalir)
            Spacer(Modifier.height(36.dp))
        }
    }
}
