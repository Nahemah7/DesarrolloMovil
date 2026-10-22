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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.data.ConvertidorImagen
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoContrasena
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.BurdeoSuave
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.viewmodel.PerfilViewModel
import java.io.File

/**
 * Perfil de quien esta usando el telefono: sus datos, la seguridad de su cuenta (cambiar la clave y
 * elegir las dos preguntas con las que la recupera).
 */
@Composable
fun PantallaPerfil(onSalir: () -> Unit) {
    val vm: PerfilViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // La camara: primero el permiso y despues el disparo. La foto vuelve ya recortada y reducida.
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

    Scaffold(
        containerColor = Hueso,
        topBar = {
            BarraApp(
                titulo = estado.nombre.ifEmpty { "Mi perfil" },
                subtitulo = estado.perfil,
                etiqueta = "Mi perfil",
                colorEtiqueta = Info
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                // Sin esto el boton "Cambiar la clave" queda debajo del teclado y el toque no llega
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Seccion("Mis datos")
            TarjetaSeccion {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(estado.nombre, estado.foto)
                    Column(Modifier.padding(start = 16.dp)) {
                        Text(estado.nombre, style = MaterialTheme.typography.titleSmall)
                        Text(estado.perfil, style = MaterialTheme.typography.bodyMedium)
                        Text(estado.rut, style = MaterialTheme.typography.bodySmall, color = Apagado)
                    }
                }
                Spacer(Modifier.height(Dimens.espacioChico))
                BotonSecundario(
                    texto = if (estado.foto == null) "Agregar una foto" else "Cambiar la foto",
                    icono = Icons.Filled.PhotoCamera,
                    onClick = { pedirFoto(permiso, tomarFoto, contexto) { bytes -> vm.guardarFoto(bytes) } }
                )
                if (estado.avisoFoto != null) AvisoBueno(estado.avisoFoto!!)
            }

            Seccion("Seguridad: cambiar tu clave")
            TarjetaSeccion {
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
                Spacer(Modifier.height(Dimens.espacioChico))
                BotonPrincipal("Cambiar la clave", onClick = { if (!vm.cambiarClave()) Unit })
            }

            Seccion("Seguridad: tus dos preguntas")
            TarjetaSeccion {
                Text(
                    if (estado.tienePreguntas) "Estas son tus preguntas. Puedes cambiarlas."
                    else "Elige dos preguntas y escribelas: con ellas recuperas tu clave si se te olvida.",
                    style = MaterialTheme.typography.bodySmall, color = Apagado
                )
                Spacer(Modifier.height(Dimens.espacioChico))
                estado.catalogo.forEach { pregunta ->
                    val elegida = estado.elegidas.contains(pregunta.id)
                    Row(
                        Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = elegida, onCheckedChange = { vm.onAlternarPregunta(pregunta.id) })
                        Text(pregunta.texto, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    }
                    if (elegida) {
                        CampoTexto(
                            valor = estado.respuestas[pregunta.id] ?: "",
                            onValorCambia = { vm.onRespuestaCambia(pregunta.id, it) },
                            etiqueta = "Tu respuesta",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                if (estado.errorPreguntas != null) AvisoMal(estado.errorPreguntas!!)
                if (estado.avisoPreguntas != null) AvisoBueno(estado.avisoPreguntas!!)
                Spacer(Modifier.height(Dimens.espacioChico))
                BotonPrincipal("Guardar las preguntas", onClick = { if (!vm.guardarPreguntas()) Unit })
            }

            Spacer(Modifier.height(14.dp))
            BotonPrincipal("Cerrar sesión", onClick = onSalir)
            Spacer(Modifier.height(36.dp))
        }
    }
}

/**
 * Avatar del perfil: la foto si la tiene, y si no las iniciales. Es el reconocimiento visual de
 * quien esta usando el telefono, no un dato del conteo.
 */
@Composable
private fun Avatar(nombre: String, foto: ByteArray?) {
    val iniciales = nombre.split(" ").filter { it.isNotBlank() }.take(2)
        .map { it.first().uppercaseChar() }.joinToString("")
    val imagen = remember(foto) { foto?.let { ConvertidorImagen.aBitmap(it) } }
    Box(
        Modifier.size(80.dp).clip(RoundedCornerShape(99.dp)).background(BurdeoSuave),
        contentAlignment = Alignment.Center
    ) {
        if (imagen != null) {
            Image(
                bitmap = imagen.asImageBitmap(),
                contentDescription = "Tu foto de perfil",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(99.dp))
            )
        } else {
            Text(
                text = iniciales.ifEmpty { "?" },
                color = Burdeo, fontWeight = FontWeight.Bold, fontSize = 26.sp
            )
        }
    }
}

/** Pide el permiso si falta y abre la camara; devuelve la foto ya recortada y reducida. */
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
