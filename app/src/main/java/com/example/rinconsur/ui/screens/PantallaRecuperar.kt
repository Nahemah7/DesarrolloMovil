package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.PasoRecuperar
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoLista
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.viewmodel.RecuperarViewModel
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun PantallaRecuperar(onVolver: () -> Unit) {
    val vm: RecuperarViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = Fondo) { pad ->
        Column(
            Modifier.padding(pad).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(Dimens.espacioGrande))
            Text("RINCÓN SUR", style = MaterialTheme.typography.labelMedium, color = Marino)
            Text("Recuperar tu clave", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(Dimens.espacioMedio))

            when (estado.paso) {
                PasoRecuperar.RUT -> {
                    Seccion("Tu RUT")
                    CampoTexto(
                        valor = estado.rut,
                        onValorCambia = vm::onRutCambia,
                        etiqueta = "RUT",
                        error = estado.errorRut,
                        marcador = "19.876.345-4"
                    )
                    BloqueAviso(estado.errorGeneral)
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    BotonPrincipal("Continuar", onClick = { if (!vm.buscar()) Unit })
                    Spacer(Modifier.height(Dimens.espacioChico))
                    BotonSecundario("Volver al ingreso", onClick = onVolver)
                }

                PasoRecuperar.PREGUNTAS -> {
                    Seccion("Tu pregunta de seguridad")
                    Nota("Elige la pregunta que guardaste en tu perfil y respóndela.")
                    estado.elegidas.forEachIndexed { i, posicion ->
                        CampoLista(
                            etiqueta = if (estado.elegidas.size == 1) "Pregunta" else "Pregunta " + (i + 1),
                            textoElegido = estado.catalogo.getOrNull(posicion)?.texto ?: "",
                            opciones = estado.catalogo.map { it.texto },
                            alElegir = { vm.onPreguntaElegida(i, it) },
                            marcador = "Elige tu pregunta"
                        )
                        Spacer(Modifier.height(Dimens.espacioMini))
                        CampoTexto(
                            valor = estado.respuestas.getOrElse(i) { "" },
                            onValorCambia = { vm.onRespuestaCambia(i, it) },
                            etiqueta = "Respuesta",
                            error = if (i == 0) estado.errorRespuestas else null
                        )
                    }
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    BotonPrincipal("Continuar", onClick = { if (!vm.verificar()) Unit })
                    Spacer(Modifier.height(Dimens.espacioChico))
                    BotonSecundario("Volver", onClick = { vm.volver() })
                }

                PasoRecuperar.CLAVE -> {
                    Seccion("Tu clave nueva")
                    CampoContrasena(
                        valor = estado.clave,
                        onValorCambia = vm::onClaveCambia,
                        etiqueta = "Clave nueva",
                        visible = estado.claveVisible,
                        onAlternarVisibilidad = vm::onAlternarClave,
                        error = estado.errorClave
                    )
                    CampoContrasena(
                        valor = estado.repetir,
                        onValorCambia = vm::onRepetirCambia,
                        etiqueta = "Repite la clave nueva",
                        visible = estado.claveVisible,
                        onAlternarVisibilidad = vm::onAlternarClave,
                        error = estado.errorRepetir
                    )
                    Nota("Mínimo 8 caracteres, con mayúscula, minúscula y número.")
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    BotonPrincipal("Cambiar la clave", onClick = { if (!vm.cambiar()) Unit })
                    Spacer(Modifier.height(Dimens.espacioChico))
                    BotonSecundario("Volver", onClick = { vm.volver() })
                }

                PasoRecuperar.LISTO -> {
                    TarjetaSeccion {
                        Text("Tu clave quedó cambiada.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        Nota("Ya puedes entrar con tu RUT y la clave nueva.")
                    }
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    BotonPrincipal("Volver al ingreso", onClick = onVolver)
                }
            }
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}

@Composable
private fun BloqueAviso(texto: String?) {
    if (texto == null) return
    Spacer(Modifier.height(Dimens.espacioChico))
    TarjetaSeccion {
        androidx.compose.foundation.layout.Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Aviso)
            Text("  " + texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}
