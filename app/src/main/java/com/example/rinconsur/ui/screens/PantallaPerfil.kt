package com.example.rinconsur.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.data.ConvertidorImagen
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoLista
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.AquaSuave
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.AvisoSuave
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Fondo
import com.example.rinconsur.ui.theme.Linea
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Superficie
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.PerfilViewModel
import java.io.File

@Composable
fun PantallaPerfil(onSalir: () -> Unit, onDatosCambiados: () -> Unit = {}) {
    val vm: PerfilViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var cambiandoNombre by remember { mutableStateOf(false) }
    var cambiandoClave by remember { mutableStateOf(false) }

    val tomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { salioBien: Boolean ->
        if (salioBien) {
            val archivo = File(File(contexto.cacheDir, "fotos"), "perfil.jpg")
            ConvertidorImagen.desdeUri(contexto, Uri.fromFile(archivo))?.let { foto ->
                vm.guardarFoto(ConvertidorImagen.aBytes(foto))
            }
        }
        vm.limpiarAvisoFoto()
    }
    val permiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (!concedido) vm.onPermisoDenegado()
    }

    Scaffold(containerColor = Fondo) { pad ->
        Column(
            Modifier
                .padding(pad)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            // Salir es una acción secundaria: va arriba, lejos del pulgar y con borde.
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                    onClick = onSalir,
                    shape = RoundedCornerShape(Dimens.radioEtiqueta),
                    // Mismo azul marino del botón principal que tenía antes.
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Marino,
                        contentColor = Superficie
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Cerrar sesión",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Superficie
                    )
                }
            }
            Spacer(Modifier.height(Dimens.espacioChico))

            // Los datos de la persona viven en su tarjeta: foto, nombre y cargo a la izquierda,
            // y la acción de cambiar el nombre al costado, no debajo.
            TarjetaSeccion {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AvatarConCamara(
                        nombre = estado.nombre + " " + estado.apellido,
                        foto = estado.foto,
                        alTocarCamara = {
                            pedirFoto(permiso, tomarFoto, contexto) { bytes -> vm.guardarFoto(bytes) }
                        }
                    )
                    Column(Modifier.weight(1f).padding(start = Dimens.espacioChico)) {
                        Text(
                            text = (estado.nombre + " " + estado.apellido).trim(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Tinta
                        )
                        Text(
                            text = estado.cargo.ifBlank { estado.perfil },
                            style = MaterialTheme.typography.bodySmall,
                            color = Apagado
                        )
                        Text(
                            text = estado.rut,
                            style = MaterialTheme.typography.labelSmall,
                            color = Apagado
                        )
                    }
                    Box(
                        Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AquaSuave)
                            .clickable { cambiandoNombre = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Cambiar nombre",
                            tint = Marino,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (estado.avisoFoto != null) AvisoBueno(estado.avisoFoto!!)
                if (estado.avisoNombre != null) AvisoBueno(estado.avisoNombre!!)
            }

            Seccion("Seguridad")
            TarjetaSeccion {
                FilaDato(
                    titulo = "Clave",
                    valor = "••••••••••",
                    accion = "Cambiar clave",
                    alTocarAccion = { cambiandoClave = true }
                )
                HorizontalDivider(color = Linea, thickness = 1.dp)
                FilaDato(
                    titulo = "Teléfono",
                    valor = estado.telefono?.takeIf { it.isNotBlank() } ?: "Sin registrar"
                )
                HorizontalDivider(color = Linea, thickness = 1.dp)
                FilaDato(titulo = "Cargo", valor = estado.perfil.ifBlank { "—" })
            }

            val idElegida = estado.elegidas.firstOrNull()
            val textoElegido = estado.catalogo.firstOrNull { it.id == idElegida }?.texto ?: ""
            val respuestaElegida = idElegida?.let { estado.respuestas[it] } ?: ""

            Seccion("Pregunta de seguridad")
            TarjetaSeccion {
                Text(
                    if (estado.tienePreguntas) "Esta es tu pregunta. Puedes cambiarla cuando quieras."
                    else "Elige una pregunta y escríbela: con ella recuperas tu clave si se te olvida.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Apagado
                )
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoLista(
                    etiqueta = "Pregunta",
                    textoElegido = textoElegido,
                    opciones = estado.catalogo.map { it.texto },
                    alElegir = { indice -> vm.onElegirPregunta(estado.catalogo[indice].id) }
                )
                Spacer(Modifier.height(Dimens.espacioChico))
                CampoTexto(
                    valor = respuestaElegida,
                    onValorCambia = { vm.onRespuestaCambia(idElegida ?: 0L, it) },
                    etiqueta = "Respuesta",
                    modifier = Modifier.fillMaxWidth()
                )
                if (estado.errorPreguntas != null) AvisoMal(estado.errorPreguntas!!)
                if (estado.avisoPreguntas != null) AvisoBueno(estado.avisoPreguntas!!)
                Spacer(Modifier.height(Dimens.espacioChico))
                BotonPrincipal("Guardar la pregunta", onClick = { vm.guardarPreguntas() })
            }

            Spacer(Modifier.height(Dimens.espacioGrande))
        }
    }

    if (cambiandoNombre) {
        AlertDialog(
            onDismissRequest = {
                cambiandoNombre = false
                vm.cancelarCambioNombre()
            },
            title = { Text("Cambiar nombre") },
            text = {
                Column {
                    CampoTexto(
                        valor = estado.nombre,
                        onValorCambia = vm::onNombreCambia,
                        etiqueta = "Nombre",
                        error = estado.errorNombre,
                        etiquetaArriba = true
                    )
                    Spacer(Modifier.height(Dimens.espacioMedio))
                    CampoTexto(
                        valor = estado.apellido,
                        onValorCambia = vm::onApellidoCambia,
                        etiqueta = "Apellido",
                        etiquetaArriba = true
                    )
                    Nota("El RUT no se cambia: identifica tu cuenta.")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (vm.guardarNombre()) {
                        cambiandoNombre = false
                        onDatosCambiados()
                    }
                }) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    cambiandoNombre = false
                    vm.cancelarCambioNombre()
                }) { Text("Cancelar") }
            }
        )
    }

    if (cambiandoClave) {
        AlertDialog(
            onDismissRequest = { cambiandoClave = false },
            title = { Text("Cambiar la clave") },
            text = {
                Column {
                    CampoContrasena(
                        valor = estado.claveActual,
                        onValorCambia = vm::onClaveActualCambia,
                        etiqueta = "Clave actual",
                        visible = estado.claveVisible,
                        onAlternarVisibilidad = vm::onAlternarClave,
                        error = estado.errorActual
                    )
                    CampoContrasena(
                        valor = estado.claveNueva,
                        onValorCambia = vm::onClaveNuevaCambia,
                        etiqueta = "Clave nueva",
                        visible = estado.claveVisible,
                        onAlternarVisibilidad = vm::onAlternarClave,
                        error = estado.errorNueva
                    )
                    CampoContrasena(
                        valor = estado.repetirNueva,
                        onValorCambia = vm::onRepetirCambia,
                        etiqueta = "Repite la clave nueva",
                        visible = estado.claveVisible,
                        onAlternarVisibilidad = vm::onAlternarClave,
                        error = estado.errorRepetir
                    )
                    Nota("Mínimo 8 caracteres, con mayúscula, minúscula y número.")
                    if (estado.avisoClave != null) AvisoBueno(estado.avisoClave!!)
                }
            },
            confirmButton = {
                TextButton(onClick = { if (vm.cambiarClave()) cambiandoClave = false }) {
                    Text("Cambiar la clave")
                }
            },
            dismissButton = {
                TextButton(onClick = { cambiandoClave = false }) { Text("Cancelar") }
            }
        )
    }
}

// Una fila de la ficha: qué es, su valor y, si se puede tocar, la acción a la derecha.
@Composable
private fun FilaDato(
    titulo: String,
    valor: String,
    accion: String? = null,
    alTocarAccion: () -> Unit = {}
) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.bodySmall, color = Apagado)
            Text(
                text = valor,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Tinta
            )
        }
        if (accion != null) {
            Spacer(Modifier.width(Dimens.espacioMini))
            TextButton(onClick = alTocarAccion) {
                Text(
                    text = accion,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Marino
                )
            }
        }
    }
}

@Composable
private fun AvatarConCamara(nombre: String, foto: ByteArray?, alTocarCamara: () -> Unit) {
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .map { it.first().uppercaseChar() }.joinToString("")
    val imagen = remember(foto) { foto?.let { ConvertidorImagen.aBitmap(it) } }

    Box {
        Box(contentAlignment = Alignment.BottomEnd) {
            Box(
                Modifier.size(72.dp).clip(CircleShape).background(AquaSuave),
                contentAlignment = Alignment.Center
            ) {
                if (imagen != null) {
                    Image(
                        bitmap = imagen.asImageBitmap(),
                        contentDescription = "Tu foto de perfil",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(72.dp).clip(CircleShape)
                    )
                } else {
                    Text(
                        text = iniciales.ifEmpty { "?" },
                        color = Marino,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                }
            }
            Box(
                Modifier
                    .padding(end = 2.dp, bottom = 2.dp)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Marino)
                    .clickable { alTocarCamara() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.PhotoCamera,
                    contentDescription = "Cambiar la foto",
                    tint = Superficie,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

private fun pedirFoto(
    permiso: ManagedActivityResultLauncher<String, Boolean>,
    camara: ManagedActivityResultLauncher<Uri, Boolean>,
    contexto: Context,
    alTenerla: (ByteArray) -> Unit
) {
    fun disparar() {
        val carpeta = File(contexto.cacheDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "perfil.jpg")
        val uri = FileProvider.getUriForFile(contexto, contexto.packageName + ".archivos", archivo)
        camara.launch(uri)
    }
    if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED
    ) disparar() else permiso.launch(Manifest.permission.CAMERA)
}

@Composable
private fun AvisoMal(texto: String) {
    Spacer(Modifier.height(Dimens.espacioChico))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Filled.Warning, contentDescription = null, tint = Aviso)
        Text("  " + texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Aviso)
    }
}

@Composable
private fun AvisoBueno(texto: String) {
    Spacer(Modifier.height(Dimens.espacioChico))
    Text(texto, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Ok)
}
