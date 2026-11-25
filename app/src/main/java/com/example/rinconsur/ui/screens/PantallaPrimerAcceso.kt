package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.PrimerAccesoViewModel

@Composable
fun PantallaPrimerAcceso(onListo: (PerfilUsuario) -> Unit) {
    val vm: PrimerAccesoViewModel = viewModel()
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    LaunchedEffect(Unit) { vm.aviso.collect { host.showSnackbar(it) } }

    Scaffold(containerColor = Fondo, snackbarHost = { SnackbarHost(host) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            Spacer(Modifier.height(Dimens.espacioMedio))
            Text(
                text = if (vm.nombre.isBlank()) "Primer acceso" else "Hola, " + vm.nombre,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = Tinta
            )
            Spacer(Modifier.height(Dimens.espacioMini))
            Nota("Tu cuenta se creó con un código temporal. Define tu contraseña y tu pregunta de seguridad para seguir.")

            Seccion("Tu contraseña")
            TarjetaSeccion {
                CampoTexto(
                    valor = ui.clave,
                    onValorCambia = vm::onClaveCambia,
                    etiqueta = "Contraseña nueva",
                    error = ui.errorClave,
                    tipoTeclado = KeyboardType.Password,
                    etiquetaArriba = true
                )
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(
                    valor = ui.repetir,
                    onValorCambia = vm::onRepetirCambia,
                    etiqueta = "Repite la contraseña",
                    error = ui.errorRepetir,
                    tipoTeclado = KeyboardType.Password,
                    etiquetaArriba = true
                )
                Spacer(Modifier.height(Dimens.espacioMini))
                Nota("Al menos 8 caracteres, con mayúscula, minúscula y número.")
            }

            Seccion("Tu pregunta de seguridad")
            TarjetaSeccion {
                Nota("Se usa solo si olvidas tu contraseña. Elige una y guarda la respuesta.")
                Spacer(Modifier.height(Dimens.espacioMini))
                ui.catalogo.forEach { pregunta ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = ui.idPregunta == pregunta.id,
                                onClick = { vm.onPreguntaElegida(pregunta.id) }
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = ui.idPregunta == pregunta.id,
                            onClick = { vm.onPreguntaElegida(pregunta.id) }
                        )
                        Text(
                            text = pregunta.texto,
                            style = MaterialTheme.typography.bodySmall,
                            color = Tinta,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
                if (ui.errorPregunta != null) {
                    Text(
                        text = ui.errorPregunta!!,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(
                    valor = ui.respuesta,
                    onValorCambia = vm::onRespuestaCambia,
                    etiqueta = "Respuesta",
                    error = ui.errorRespuesta,
                    maximo = 60,
                    etiquetaArriba = true
                )
            }

            Spacer(Modifier.height(Dimens.espacioMedio))
            BotonPrincipal(
                texto = "Guardar y entrar",
                onClick = { vm.guardar { perfil -> if (perfil != null) onListo(perfil) } },
                habilitado = ui.puedeGuardar
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
