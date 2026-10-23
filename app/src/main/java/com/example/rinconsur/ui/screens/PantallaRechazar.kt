package com.example.rinconsur.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.data.SesionPreferencias
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.RechazarViewModel

/**
 * Rechazar una sesión (supervisor). Se marcan uno o más motivos en casillas; si se marca
 * "Otro motivo" aparece una caja de texto que hay que llenar. Los errores se muestran junto
 * al campo que los causó (validación por campo) y la validación vive en el ViewModel.
 */
@Composable
fun PantallaRechazar(idSesion: Long, onVolver: () -> Unit) {
    val vm: RechazarViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    // Quién revisa: la persona que tiene la sesión iniciada en el teléfono
    val contexto = LocalContext.current
    val idRevisor = remember { SesionPreferencias(contexto).leer() }

    // Al abrir la pantalla se cargan los datos de la sesión que se va a rechazar
    LaunchedEffect(idSesion) { vm.cargar(idSesion) }

    Scaffold(
        containerColor = Hueso,
        topBar = { BarraApp("Rechazar sesión", estado.codigo, "Supervisor", Info) }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ---- Datos de la sesión ----
            Seccion("Sesión")
            TarjetaSeccion {
                Text(estado.acceso, style = MaterialTheme.typography.bodyMedium, color = Tinta)
                Text(
                    estado.hora + " · " + estado.observador,
                    style = MaterialTheme.typography.bodySmall,
                    color = Apagado
                )
                val pct = estado.pctTransito?.let { String.format("%.1f %%", it) } ?: "sin dato"
                Nota(estado.registros.toString() + " registros · tránsito " + pct)
            }

            // ---- Motivos (casillas) ----
            Seccion("Motivo del rechazo")
            TarjetaSeccion {
                vm.motivos.forEach { motivo ->
                    val marcado = estado.motivos.contains(motivo)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { vm.onMotivo(motivo) }
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Checkbox(checked = marcado, onCheckedChange = { vm.onMotivo(motivo) })
                        Text(motivo.etiqueta, style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    }
                }
                // Error del grupo de casillas (null = no se muestra nada)
                estado.errorMotivos?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = Aviso)
                }
            }

            // ---- "Otro motivo": la caja de texto solo existe si se marcó esa casilla ----
            if (estado.exigeOtro) {
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = estado.otroMotivo,
                    onValorCambia = { vm.onOtroMotivo(it) },
                    etiqueta = "Cuenta el motivo",
                    modifier = Modifier.fillMaxWidth(),
                    error = estado.errorOtro,
                    lineas = 3,
                    maximo = 200
                )
            }

            Nota("El rechazo no borra nada: los registros se conservan para volver a contar ese bloque.")

            // ---- Botones ----
            Spacer(Modifier.height(14.dp))
            BotonPrincipal(
                texto = "Rechazar sesión",
                onClick = { if (vm.rechazar(idSesion, idRevisor)) onVolver() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            BotonSecundario(
                texto = "Cancelar",
                onClick = onVolver,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}