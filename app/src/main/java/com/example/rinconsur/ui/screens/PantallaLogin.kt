package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.*
import com.example.rinconsur.ui.components.*
import com.example.rinconsur.ui.theme.*
import com.example.rinconsur.viewmodel.*

/** Inicio de sesion: valida el RUT contra la base y recuerda quien entro. */
@Composable
fun PantallaLogin(onEntrar: (PerfilUsuario) -> Unit) {
    val vm: LoginViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(containerColor = Color.White) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            // Espacio para que el encabezado no quede pegado a la barra de estado del telefono
            Spacer(Modifier.height(36.dp))
            Text("RINCON SUR", style = MaterialTheme.typography.labelMedium, color = Burdeo)
            Text("Control de flujo\npor acceso", style = MaterialTheme.typography.titleLarge)
            Nota("Uso interno. Cada colaborador entra con su propia cuenta.")
            Seccion("RUT")
            CampoTexto(
                valor = estado.rut,
                onValorCambia = vm::onRutCambia,
                etiqueta = "RUT",
                error = estado.errorRut,
                marcador = "19.876.345-4",
                icono = Icons.Filled.Person
            )
            Seccion("Contrasena")
            CampoContrasena(
                valor = estado.clave,
                onValorCambia = vm::onClaveCambia,
                etiqueta = "Contrasena",
                visible = estado.claveVisible,
                onAlternarVisibilidad = vm::onAlternarClave,
                error = estado.errorClave
            )
            // El error de autenticacion va arriba del boton y es uno solo, sin decir cual campo fallo
            if (estado.errorGeneral != null) {
                TarjetaSeccion(acento = Aviso, modifier = Modifier.padding(top = Dimens.espacioChico)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Warning, contentDescription = null, tint = Aviso)
                        Text(
                            "  " + (estado.errorGeneral ?: ""),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Nota("Por seguridad la app no dice cual de los dos datos fallo.")
                }
            }
            Spacer(Modifier.height(Dimens.espacioMedio))
            BotonPrincipal(
                texto = "Entrar",
                icono = Icons.Filled.Login,
                onClick = {
                    val rol = vm.entrar()
                    if (rol != null) onEntrar(rol)
                }
            )
            Nota("El telefono se comparte entre turnos: la sesion se cierra al devolver el equipo.")
            Seccion("Colaboradores de ejemplo")
            TarjetaSeccion {
                Text("19.876.345-4 · Tamara Rojas · observadora", style = MaterialTheme.typography.bodySmall)
                Text("17.654.320-5 · Daniela Pizarro · supervisora", style = MaterialTheme.typography.bodySmall)
                Text("13.987.442-0 · Rodrigo Saavedra · subgerente", style = MaterialTheme.typography.bodySmall)
                Nota("La clave de los tres es la misma en este prototipo: " + CLAVE_DE_EJEMPLO + ". La app guarda su hash, nunca la clave.")
            }
            // Colchon final: los ultimos datos no quedan debajo de los botones del sistema
            Spacer(Modifier.height(36.dp))
        }
    }
}

/** Clave de los colaboradores de ejemplo. Se muestra porque es un prototipo: en produccion no. */
private const val CLAVE_DE_EJEMPLO = "rincon2026"
