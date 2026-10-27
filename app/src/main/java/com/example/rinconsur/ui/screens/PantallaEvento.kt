package com.example.rinconsur.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.example.rinconsur.model.EventoDia
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.data.TEXTO_MAXIMO
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

    // Evento que se esta anulando: el dialogo pide el motivo y el evento no se borra.
    var eventoAAnular by remember { mutableStateOf<EventoDia?>(null) }

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
                    maximo = TEXTO_MAXIMO
                )
            }

            // ---- Foto de evidencia (opcional) ----
            Seccion("Foto de evidencia (opcional)")
            TarjetaSeccion {
                val imagen = remember(estado.fotoBytes) {
                    estado.fotoBytes?.let { ConvertidorImagen.aBitmap(it) }
                }
                AnimatedVisibility(
                    visible = imagen != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    imagen?.let { img ->
                        Column {
                            Image(
                                bitmap = img.asImageBitmap(),
                                contentDescription = "Foto de evidencia del evento",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                            Spacer(Modifier.height(8.dp))
                            BotonSecundario("Quitar foto", onClick = { vm.quitarFoto() }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }

                AnimatedVisibility(
                    visible = imagen == null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
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

            // Avisos con animación (Tarea #20): permiso denegado o "Evento E-08 guardado"
            AnimatedVisibility(
                visible = estado.mensaje != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                estado.mensaje?.let { mensaje ->
                    Nota(mensaje)
                }
            }

            // ---- Botones ----
            Spacer(Modifier.height(14.dp))
            BotonPrincipal(
                texto = "Guardar evento",
                onClick = { vm.guardar() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            // ---- Lo registrado hoy: el evento no desaparece al guardarlo ----
            Spacer(Modifier.height(20.dp))
            Seccion("Eventos registrados · " + estado.eventos.size)
            if (estado.eventos.isEmpty()) {
                TarjetaSeccion { Nota("Todavía no hay eventos registrados.") }
            }

            val hoy = hoyIso()
            estado.eventos.forEach { evento ->
                Spacer(Modifier.height(8.dp))
                TarjetaSeccion {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                evento.codigo + " · " + evento.tipo.etiqueta,
                                style = MaterialTheme.typography.titleSmall,
                                color = Tinta
                            )

                            val acceso = evento.idAcceso
                                ?.let { id -> vm.accesos.firstOrNull { it.id == id }?.etiquetaCorta }
                            val fechas = if (evento.fechaInicio == evento.fechaFin) fechaCorta(evento.fechaInicio)
                                else fechaCorta(evento.fechaInicio) + " a " + fechaCorta(evento.fechaFin)
                            Nota(
                                listOf(fechas, acceso, evento.nombreRegistra)
                                    .filterNotNull()
                                    .filter { it.isNotBlank() }
                                    .joinToString(" · ")
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                evento.descripcion,
                                style = MaterialTheme.typography.bodySmall,
                                color = Tinta
                            )

                            // Anulado manda; si no, la etiqueta dice si el evento ya paso o esta por venir
                            val etiqueta = when {
                                evento.anulado -> "Anulado" to "aviso"
                                evento.fechaFin < hoy -> "Finalizado" to "neutro"
                                else -> "Programado" to "info"
                            }
                            Spacer(Modifier.height(4.dp))
                            EtiquetaEstado(etiqueta.first, etiqueta.second)
                            if (evento.anulado) {
                                Nota("Motivo: " + (evento.motivoAnulacion ?: "sin detalle"))
                            }
                        }

                        // La foto de evidencia, chica: se ve que esta sin abrir el evento
                        val miniatura = remember(evento.id, evento.anulado) {
                            evento.foto?.let { ConvertidorImagen.aBitmap(it) }
                        }
                        miniatura?.let { img ->
                            Spacer(Modifier.height(0.dp))
                            Image(
                                bitmap = img.asImageBitmap(),
                                contentDescription = "Foto del evento " + evento.codigo,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        }
                    }

                    if (!evento.anulado) {
                        Spacer(Modifier.height(8.dp))
                        BotonSecundario(
                            "Anular con motivo",
                            onClick = { eventoAAnular = evento },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }

    // Anular: pide el motivo; el evento queda en la lista con el motivo y su rastro.
    eventoAAnular?.let { evento ->
        var motivo by remember(evento.id) { mutableStateOf("") }
        var errorMotivo by remember(evento.id) { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { eventoAAnular = null },
            title = { Text("Anular " + evento.codigo) },
            text = {
                Column {
                    Nota("El evento no se borra: queda en la lista con el motivo y sale del conteo del día.")
                    Spacer(Modifier.height(10.dp))
                    CampoTexto(
                        valor = motivo,
                        onValorCambia = { motivo = it; errorMotivo = null },
                        etiqueta = "Motivo de la anulación",
                        etiquetaArriba = true,
                        error = errorMotivo,
                        lineas = 3,
                        maximo = TEXTO_MAXIMO
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val error = vm.anular(evento.id, motivo)
                    if (error == null) eventoAAnular = null else errorMotivo = error
                }) { Text("Anular") }
            },
            dismissButton = {
                TextButton(onClick = { eventoAAnular = null }) { Text("Cancelar") }
            }
        )
    }
}

/** La fecha de hoy como se guarda en la base: sirve para saber si el evento ya paso. */
private fun hoyIso(): String =
    java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())

/** 2026-10-26 -> 26-10-2026: la fecha como se lee en el resto de la app. */
private fun fechaCorta(iso: String): String {
    val partes = iso.split("-")
    return if (partes.size == 3) partes[2] + "-" + partes[1] + "-" + partes[0] else iso
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
