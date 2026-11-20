package com.example.rinconsur.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.ConexionViewModel
import com.example.rinconsur.viewmodel.EstadoPaso

// Pantalla de diagnóstico: se abre desde el perfil y muestra, paso por paso, si la app llegó al
// backend del PC. Sirve para separar "no hay red" de "el servicio está caído".
@Composable
fun PantallaConexion(onVolver: () -> Unit) {
    val vm: ConexionViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = Fondo) { pad ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            BarraApp(titulo = "Conexión", subtitulo = "La app con el servidor del equipo")

            Spacer(Modifier.height(Dimens.espacioChico))

            Seccion("Dirección del servidor")
            TarjetaSeccion {
                CampoTexto(
                    valor = estado.direccion,
                    onValorCambia = vm::onDireccion,
                    etiqueta = "Dirección",
                    marcador = "http://localhost:8090",
                    habilitado = !estado.probando
                )
                Spacer(Modifier.height(Dimens.espacioMini))
                Nota(
                    "Por defecto va por el túnel de adb (adb reverse tcp:8090 tcp:8090) con " +
                        "http://localhost:8090: sirve en el emulador y en un teléfono por USB. " +
                        "Por la red local, usa la IP del PC, por ejemplo http://192.168.1.40:8090."
                )
            }

            Spacer(Modifier.height(Dimens.espacioChico))
            BotonPrincipal(
                texto = if (estado.probando) "Probando…" else "Probar conexión",
                onClick = vm::probar,
                habilitado = !estado.probando
            )

            Spacer(Modifier.height(Dimens.espacioMedio))
            Seccion("Resultado")
            TarjetaSeccion {
                estado.pasos.forEachIndexed { indice, paso ->
                    if (indice > 0) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Linea)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 10.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = paso.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Tinta
                            )
                            if (paso.detalle.isNotBlank()) {
                                Text(
                                    text = paso.detalle,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = com.example.rinconsur.ui.theme.Apagado
                                )
                            }
                        }
                        Box(
                            Modifier
                                .padding(start = Dimens.espacioMini)
                                .clip(RoundedCornerShape(Dimens.radioEtiqueta))
                                .background(
                                    when (paso.estado) {
                                        EstadoPaso.OK -> Aqua
                                        EstadoPaso.ERROR -> Aviso
                                        EstadoPaso.PENDIENTE -> Linea
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = when (paso.estado) {
                                    EstadoPaso.OK -> "${paso.milisegundos} ms"
                                    EstadoPaso.ERROR -> "falló"
                                    EstadoPaso.PENDIENTE -> "sin probar"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = when (paso.estado) {
                                    EstadoPaso.PENDIENTE -> Tinta
                                    else -> com.example.rinconsur.ui.theme.Superficie
                                }
                            )
                        }
                    }
                }
            }

            estado.mensaje?.let {
                Spacer(Modifier.height(Dimens.espacioChico))
                Nota(it)
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}
