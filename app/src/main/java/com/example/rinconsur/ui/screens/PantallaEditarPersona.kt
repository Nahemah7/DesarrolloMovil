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
import androidx.compose.material3.Switch
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
import com.example.rinconsur.model.Genero
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
import com.example.rinconsur.viewmodel.EditarPersonaViewModel

@Composable
fun PantallaEditarPersona(idUsuario: Long, onVolver: () -> Unit) {
    val vm: EditarPersonaViewModel = viewModel()
    val persona by vm.persona.collectAsStateWithLifecycle()
    val perfil by vm.perfil.collectAsStateWithLifecycle()
    val activo by vm.activo.collectAsStateWithLifecycle()
    val rut by vm.rut.collectAsStateWithLifecycle()
    val errores by vm.errores.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }

    LaunchedEffect(idUsuario) { vm.cargar(idUsuario) }
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
            Nota("Cuenta de " + rut + ". El RUT no se cambia: es con lo que la persona entra.")

            Seccion("Datos")
            TarjetaSeccion {
                CampoTexto(persona.nombres, vm::onNombresCambia, "Nombres", etiquetaArriba = true)
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(persona.apellidoPaterno, vm::onApellidoCambia, "Apellido paterno", etiquetaArriba = true)
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(persona.apellidoMaterno, vm::onApellidoMaternoCambia, "Apellido materno", etiquetaArriba = true)
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(
                    persona.correo, vm::onCorreoCambia, "Correo",
                    etiquetaArriba = true, tipoTeclado = KeyboardType.Email
                )
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(
                    persona.telefono, vm::onTelefonoCambia, "Teléfono (opcional)",
                    etiquetaArriba = true, tipoTeclado = KeyboardType.Phone, maximo = 20
                )
            }

            Seccion("Género")
            TarjetaSeccion {
                Genero.entries.forEach { genero ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = persona.genero == genero,
                                onClick = { vm.onGeneroCambia(genero) }
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = persona.genero == genero, onClick = { vm.onGeneroCambia(genero) })
                        Text(
                            text = genero.etiqueta,
                            style = MaterialTheme.typography.bodySmall,
                            color = Tinta,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }
            }

            Seccion("Perfil y cargo")
            TarjetaSeccion {
                PerfilUsuario.entries.forEach { opcion ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(selected = perfil == opcion, onClick = { vm.onPerfilCambia(opcion) })
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = perfil == opcion, onClick = { vm.onPerfilCambia(opcion) })
                        Column(Modifier.padding(start = 6.dp)) {
                            Text(
                                text = opcion.etiqueta(persona.genero),
                                style = MaterialTheme.typography.bodySmall,
                                color = Tinta
                            )
                            Text(opcion.codigo, style = MaterialTheme.typography.labelSmall, color = Apagado)
                        }
                    }
                }
                Nota("Al cambiar el perfil, el cargo se actualiza con él.")
            }

            Seccion("Estado de la cuenta")
            TarjetaSeccion {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = if (activo) "Cuenta activa" else "Cuenta desactivada",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Tinta
                        )
                        Nota(
                            if (activo) "Puede entrar y medir."
                            else "No puede entrar. Sus registros y sesiones se conservan."
                        )
                    }
                    Switch(checked = activo, onCheckedChange = vm::onActivoCambia)
                }
            }

            if (errores.isNotEmpty()) {
                Spacer(Modifier.height(Dimens.espacioChico))
                errores.forEach { falta ->
                    Text(
                        text = falta,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))
            BotonPrincipal(
                texto = "Guardar cambios",
                onClick = { if (vm.guardar()) onVolver() }
            )
            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }
}
