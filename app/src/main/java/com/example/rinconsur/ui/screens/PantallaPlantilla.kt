package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import com.example.rinconsur.ui.components.MensajeFlotante
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.Filtro
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.styles.EstilosBoton
import com.example.rinconsur.ui.styles.estiloAltoBoton
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Peligro
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.viewmodel.PlantillaViewModel

/**
 * La hoja de una plantilla de prerellenado: aca se ajustan sus valores y se guardan. Es el editor
 * al que se entra manteniendo apretado el numero en la pantalla de registro.
 */
@Composable
fun PantallaPlantilla(numero: Int, onVolver: () -> Unit) {
    val vm: PlantillaViewModel = viewModel()
    LaunchedEffect(numero) { vm.cargar(numero) }
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Hueso,
    ) { pad ->
        Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.padding(pad).navigationBarsPadding()
                .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Seccion("Clasificación")
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Clasificacion.entries.forEach { c ->
                    Filtro(c.etiqueta, estado.clasificacion == c) { vm.onClasificacion(c) }
                }
            }

            Seccion("Apariencia (la edad no se pregunta)")
            RangoEtario.entries.chunked(3).forEachIndexed { fila, tramos ->
                if (fila > 0) Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    tramos.forEach { r ->
                        Filtro(r.etiqueta, estado.rangoEtario == r) { vm.onRango(r) }
                    }
                }
            }

            Seccion("Compañía")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Compania.entries.forEach { c ->
                    Filtro(c.etiqueta, estado.compania == c) { vm.onCompania(c) }
                }
            }

            Seccion("Compras en otra tienda")
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Filtro("Sí", estado.comprasOtraTienda) { vm.onCompras(true) }
                Filtro("No", !estado.comprasOtraTienda) { vm.onCompras(false) }
            }

            if (estado.clasificacion == Clasificacion.TRANSITO) {
                Seccion("Hacia dónde se dirige")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    estado.accesos.forEach { a ->
                        Filtro(a.codigo, estado.destino == a.id) { vm.onDestino(a.id) }
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            // Guardar y borrar juntos y centrados: separados y a lo ancho no se entendian como par
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Los dos con el mismo ancho, el mismo alto y la misma forma: son un par, no dos cajas distintas
                BotonPrincipal("Guardar", onClick = { vm.guardar() }, modifier = Modifier.width(140.dp))
                if (estado.tenia) {
                    Spacer(Modifier.width(12.dp))
                    Button(
                        onClick = { vm.borrar() },
                        modifier = Modifier.width(140.dp).estiloAltoBoton(),
                        shape = EstilosBoton.forma,
                        colors = ButtonDefaults.buttonColors(containerColor = Peligro, contentColor = Color.White),
                        contentPadding = EstilosBoton.relleno
                    ) {
                        Text("Borrar", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
        }
            MensajeFlotante(estado.mensaje)
        }
    }
}
