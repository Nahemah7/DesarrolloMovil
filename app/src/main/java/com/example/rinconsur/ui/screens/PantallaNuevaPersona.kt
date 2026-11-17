package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.NuevaPersonaViewModel

@Composable
fun PantallaNuevaPersona(onVolver: () -> Unit) {
    val vm: NuevaPersonaViewModel = viewModel()
    val persona by vm.persona.collectAsStateWithLifecycle()
    val errores by vm.errores.collectAsStateWithLifecycle()
    val codigo by vm.codigo.collectAsStateWithLifecycle()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Dimens.espacioPantalla)
    ) {
        Spacer(Modifier.height(Dimens.espacioMedio))
        Nota("La cuenta se crea con un código temporal que vence en siete días. Al entrar por primera vez, la persona deberá cambiarlo y definir su pregunta de seguridad.")

        Seccion("Datos de la persona")
        TarjetaSeccion {
            CampoTexto(persona.rut, vm::onRutCambia, "RUT", etiquetaArriba = true, maximo = 12)
            Spacer(Modifier.height(Dimens.espacioChico))
            CampoTexto(persona.nombres, vm::onNombresCambia, "Nombres", etiquetaArriba = true)
            Spacer(Modifier.height(Dimens.espacioChico))
            CampoTexto(persona.apellidoPaterno, vm::onApellidoCambia, "Apellido paterno", etiquetaArriba = true)
            Spacer(Modifier.height(Dimens.espacioChico))
            CampoTexto(persona.apellidoMaterno, vm::onApellidoMaternoCambia, "Apellido materno", etiquetaArriba = true)
        }

        Seccion("Contacto")
        TarjetaSeccion {
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

        Seccion("Perfil y cargo")
        TarjetaSeccion {
            PerfilUsuario.entries.forEach { perfil ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = persona.rol == perfil,
                            onClick = { vm.onRolCambia(perfil) }
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = persona.rol == perfil, onClick = { vm.onRolCambia(perfil) })
                    Column(Modifier.padding(start = 6.dp)) {
                        Text(perfil.etiqueta, style = MaterialTheme.typography.bodySmall, color = Tinta)
                        Text(
                            perfil.codigo + " · queda registrado con ese cargo",
                            style = MaterialTheme.typography.labelSmall,
                            color = Apagado
                        )
                    }
                }
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
        BotonPrincipal(texto = "Crear cuenta y generar código", onClick = { vm.crear() })
        Spacer(Modifier.height(Dimens.espacioGrande))
    }

    if (codigo != null) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("Código temporal") },
            text = {
                Column {
                    Text(
                        text = codigo!!,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Marino
                    )
                    Spacer(Modifier.height(8.dp))
                    Nota("Se muestra una sola vez. Entrégalo a la persona junto con su RUT; vence en siete días y deberá cambiarlo al entrar.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.cerrarCodigo()
                    onVolver()
                }) { Text("Entendido") }
            }
        )
    }
}
