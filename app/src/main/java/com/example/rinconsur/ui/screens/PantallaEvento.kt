package com.example.rinconsur.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.data.ConvertidorImagen
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.CampoTexto
import com.example.rinconsur.ui.components.FilaFiltros
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Aviso
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.viewmodel.EventoViewModel
import java.io.File

private const val LADO_FOTO_EVENTO = 512

/**
 * Registrar un evento del día (clima, feriado, cierre de un acceso...), con foto de evidencia
 * opcional. Recursos nativos: la cámara (con su permiso) y, si no hay permiso, la galería.
 * La validación vive en el EventoViewModel: aquí solo se muestra el error de cada campo.
 */
@Composable
fun PantallaEvento(onVolver: () -> Unit) {
    val vm: EventoViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    // Cámara: primero el permiso y después el disparo. La foto se reduce antes de guardarla.
    val tomarFoto = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { salioBien: Boolean ->
        if (salioBien) {
            val archivo = File(File(contexto.cacheDir, "fotos"), "evento.jpg")
            ConvertidorImagen.desdeUri(contexto, Uri.fromFile(archivo), LADO_FOTO_EVENTO)?.let { foto ->
                vm.onFoto(ConvertidorImagen.aBytes(foto))
            }
        }
    }
    val permiso = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { concedido: Boolean ->
        if (!concedido) vm.onPermisoDenegado()
    }
    // Galería: alternativa cuando no hay permiso de cámara
    val galeria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            ConvertidorImagen.desdeUri(contexto, it, LADO_FOTO_EVENTO)?.let { foto ->
                vm.onFoto(ConvertidorImagen.aBytes(foto))
            }
        }
    }

    Scaffold(
        containerColor = Hueso,
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // ---- Tipo ----
            Seccion("Tipo de evento")
            FilaFiltros(
                opciones = vm.tipos.map { it.etiqueta },
                elegido = estado.tipo.etiqueta
            ) { etiqueta ->
                vm.tipos.firstOrNull { it.etiqueta == etiqueta }?.let { vm.onTipo(it) }
            }

            // ---- Acceso afectado (obligatorio si es cierre de acceso) ----
            Seccion("Acceso afectado")
            val codigoElegido = vm.accesos.firstOrNull { it.id == estado.idAcceso }?.codigo ?: "Todos"
            FilaFiltros(
                opciones = listOf("Todos") + vm.accesos.map { it.codigo },
                elegido = codigoElegido
            ) { codigo ->
                vm.onAcceso(vm.accesos.firstOrNull { it.codigo == codigo }?.id)
            }
            estado.errorAcceso?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = Aviso)
            }

            // ---- Fecha y descripción ----
            Seccion("Detalle")
            TarjetaSeccion {
                CampoTexto(
                    valor = estado.fecha,
                    onValorCambia = { vm.onFecha(it) },
                    etiqueta = "Fecha (2026-09-15)",
                    modifier = Modifier.fillMaxWidth(),
                    error = estado.errorFecha
                )
                Spacer(Modifier.height(8.dp))
                CampoTexto(
                    valor = estado.descripcion,
                    onValorCambia = { vm.onDescripcion(it) },
                    etiqueta = "Cuenta qué pasó",
                    modifier = Modifier.fillMaxWidth(),
                    error = estado.errorDescripcion,
                    lineas = 3,
                    maximo = 200
                )
            }

            // ---- Foto de evidencia (opcional) ----
            Seccion("Foto de evidencia (opcional)")
            TarjetaSeccion {
                val imagen = remember(estado.fotoBytes) {
                    estado.fotoBytes?.let { ConvertidorImagen.aBitmap(it) }
                }
                if (imagen != null) {
                    Image(
                        bitmap = imagen.asImageBitmap(),
                        contentDescription = "Foto de evidencia del evento",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Spacer(Modifier.height(8.dp))
                    BotonSecundario("Quitar foto", onClick = { vm.quitarFoto() }, modifier = Modifier.fillMaxWidth())
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BotonSecundario(
                            "Tomar foto",
                            onClick = { pedirFotoEvento(permiso, tomarFoto, contexto) },
                            modifier = Modifier.weight(1f)
                        )
                        BotonSecundario(
                            "Galería",
                            onClick = { galeria.launch("image/*") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Avisos: permiso denegado, o "Evento E-08 guardado"
            estado.mensaje?.let { Nota(it) }

            // ---- Botones ----
            Spacer(Modifier.height(14.dp))
            BotonPrincipal(
                texto = "Guardar evento",
                onClick = { vm.guardar() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Spacer(Modifier.height(20.dp))
        }
    }
}

/** Pide el permiso de cámara si falta; si ya lo hay, abre la cámara. */
private fun pedirFotoEvento(
    permiso: ManagedActivityResultLauncher<String, Boolean>,
    camara: ManagedActivityResultLauncher<Uri, Boolean>,
    contexto: Context
) {
    fun disparar() {
        val carpeta = File(contexto.cacheDir, "fotos").apply { mkdirs() }
        val archivo = File(carpeta, "evento.jpg")
        val uri = FileProvider.getUriForFile(contexto, contexto.packageName + ".archivos", archivo)
        camara.launch(uri)
    }
    if (ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA)
        == PackageManager.PERMISSION_GRANTED
    ) disparar() else permiso.launch(Manifest.permission.CAMERA)
}

