package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.FondoAcceso
import com.example.rinconsur.ui.components.MarcaRinconSur
import com.example.rinconsur.viewmodel.LoginViewModel
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AvisoSuave
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Superficie
import com.example.rinconsur.ui.theme.Dimens

@Composable
fun PantallaLogin(onEntrar: (PerfilUsuario, Boolean) -> Unit) {
    var recuperando by rememberSaveable { mutableStateOf(false) }
    if (recuperando) {
        PantallaRecuperar(onVolver = { recuperando = false })
        return
    }

    val vm: LoginViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    FondoAcceso(Modifier.fillMaxSize()) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioGrande, vertical = Dimens.espacioGrande),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = Dimens.anchoMaximoTarjeta),
                shape = RoundedCornerShape(Dimens.radioTarjetaGrande),
                color = AquaSuave,
                shadowElevation = 12.dp
            ) {
                Column(Modifier.padding(Dimens.espacioGrande)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MarcaRinconSur(tamano = 34.dp, color = Marino, acento = Aqua)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "RINCÓN SUR",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 3.sp,
                            color = Marino
                        )
                    }

                    Spacer(Modifier.height(20.dp))

                    Text(
                        text = "Control de flujo\npor acceso",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = 34.sp,
                        color = Marino
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "Uso interno de la tienda. Cada colaborador entra con su propia cuenta.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Apagado
                    )

                    Spacer(Modifier.height(20.dp))

                    CampoTexto(
                        valor = estado.rut,
                        onValorCambia = vm::onRutCambia,
                        etiqueta = "RUT",
                        error = estado.errorRut,
                        marcador = "19.876.345-4",
                        icono = Icons.Filled.Person,
                        etiquetaArriba = true
                    )

                    Spacer(Modifier.height(12.dp))

                    CampoContrasena(
                        valor = estado.clave,
                        onValorCambia = vm::onClaveCambia,
                        etiqueta = "Contraseña",
                        visible = estado.claveVisible,
                        onAlternarVisibilidad = vm::onAlternarClave,
                        error = estado.errorClave
                    )

                    if (estado.errorGeneral != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(Dimens.radioCampo),
                            color = AvisoSuave
                        ) {
                            Row(
                                Modifier.padding(Dimens.espacioMedio - 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Warning, contentDescription = null, tint = Aviso)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = estado.errorGeneral ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Aviso
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(Dimens.espacioGrande))

                    BotonPrincipal(
                        texto = if (estado.verificando) "Verificando…" else "Entrar",
                        icono = Icons.Filled.Login,
                        habilitado = !estado.verificando,
                        onClick = {
                            vm.entrar { rol, debeCambiarClave ->
                                if (rol != null) onEntrar(rol, debeCambiarClave)
                            }
                        }
                    )

                    TextButton(
                        onClick = { recuperando = true },
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = "¿Olvidaste tu clave?",
                            color = Marino,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))

            Text(
                text = "El teléfono se comparte entre turnos: la sesión se cierra al devolver el equipo.",
                style = MaterialTheme.typography.labelSmall,
                color = Superficie.copy(alpha = 0.72f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.espacioChico)
            )
        }
    }
}
