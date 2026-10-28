package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.data.MedioConexion
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.SinSenalViewModel

@Composable
fun PantallaSinSenal(onVolver: () -> Unit) {
    val vm: SinSenalViewModel = viewModel()
    val conectado by vm.conectado.collectAsStateWithLifecycle()
    val medio by vm.medio.collectAsStateWithLifecycle()
    val porEnviar by vm.porEnviar.collectAsStateWithLifecycle()
    val mensaje by vm.mensaje.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Hueso,
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Seccion("Conexión")
            TarjetaSeccion {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (conectado) "Hay internet" else "Sin conexión",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Tinta,
                        modifier = Modifier.weight(1f)
                    )
                    EtiquetaEstado(
                        texto = if (conectado) "Conectado" else "Sin señal",
                        tipo = if (conectado) "ok" else "aviso"
                    )
                }
                if (medio != MedioConexion.SIN_CONEXION) {
                    Text("Medio: " + medio.etiqueta, style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                Nota("El teléfono guarda siempre. Lo que depende de la red es el envío.")
            }

            Seccion("Por enviar")
            TarjetaSeccion {
                if (porEnviar.isEmpty()) {
                    Nota("No hay sesiones pendientes de envío.")
                } else {
                    porEnviar.forEach { sesion ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(sesion.codigo, style = MaterialTheme.typography.bodyMedium, color = Tinta)
                                Text(
                                    sesion.fecha + " · " + sesion.horaInicioProg + "–" + sesion.horaFinProg,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Apagado
                                )
                            }
                            EtiquetaEstado("Por enviar", "info")
                        }
                    }
                }
            }

            mensaje?.let { Nota(it) }

            Spacer(Modifier.height(14.dp))
            BotonPrincipal(
                texto = "Enviar ahora",
                onClick = { vm.enviar() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            BotonSecundario(
                texto = "Reintentar conexión",
                onClick = { vm.revisarRed() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
        }
    }
}