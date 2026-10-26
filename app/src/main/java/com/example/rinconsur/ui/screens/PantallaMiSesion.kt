package com.example.rinconsur.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.EtiquetaEstado
import com.example.rinconsur.ui.components.FilaFiltros
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Hueso
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.MiSesionViewModel

private fun accesoDe(accesos: List<Acceso>, sesion: SesionResumen): String =
    accesos.firstOrNull { it.id == sesion.idAcceso }?.let { it.codigo + " · " + it.nombre } ?: "Acceso sin asignar"

private fun tipoDe(estado: EstadoSesion): String = when (estado) {
    EstadoSesion.EN_CURSO -> "ok"
    EstadoSesion.PROGRAMADA -> "info"
    else -> "neutro"
}

@Composable
fun PantallaMiSesion(onIniciar: () -> Unit, onIrA: (String) -> Unit) {
    val vm: MiSesionViewModel = viewModel()
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val sesiones by vm.sesionesDeHoy.collectAsStateWithLifecycle()
    val accesos by vm.accesos.collectAsStateWithLifecycle()

    val opcionesFiltro = listOf("Todas", "Programadas", "En curso", "Cerradas")
    var filtroSeleccionado by remember { mutableStateOf("Todas") }

    val sesionesFiltradas = sesiones.filter { s ->
        when (filtroSeleccionado) {
            "Programadas" -> s.estado == EstadoSesion.PROGRAMADA
            "En curso" -> s.estado == EstadoSesion.EN_CURSO
            "Cerradas" -> s.estado != EstadoSesion.PROGRAMADA && s.estado != EstadoSesion.EN_CURSO
            else -> true
        }
    }

    val hayEnCurso = sesiones.any { it.estado == EstadoSesion.EN_CURSO }
    val hayProgramada = sesiones.any { it.estado == EstadoSesion.PROGRAMADA }

    Scaffold(
        containerColor = Hueso,
        topBar = {
            BarraApp(
                titulo = "Mi sesión",
                subtitulo = usuario?.let { it.nombre + " · " + it.perfil.etiqueta } ?: "",
                etiqueta = when {
                    hayEnCurso -> "En curso"
                    hayProgramada -> "Programada"
                    else -> null
                },
                colorEtiqueta = if (hayEnCurso) Ok else Info
            )
        },
    ) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

            if (sesiones.isEmpty()) {
                Seccion("Hoy")
                TarjetaSeccion {
                    Text("No tienes una sesión de conteo asignada para hoy.",
                        style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text("Si te toca una, la vas a ver acá cuando el supervisor la programe.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
            } else {
                Seccion(if (sesiones.size > 1) "Mis sesiones de hoy (" + sesiones.size + ")" else "Sesión de hoy")

                FilaFiltros(
                    opciones = opcionesFiltro,
                    elegido = filtroSeleccionado,
                    onElegir = { filtroSeleccionado = it }
                )
                Spacer(Modifier.height(8.dp))

                if (sesionesFiltradas.isEmpty()) {
                    TarjetaSeccion {
                        Text(
                            "No hay sesiones para el filtro seleccionado ($filtroSeleccionado).",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Apagado
                        )
                    }
                } else {
                    sesionesFiltradas.forEach { s ->
                        TarjetaSeccion {
                            Text(accesoDe(accesos, s), style = MaterialTheme.typography.titleSmall, color = Tinta)
                            Text("Hoy " + s.horaInicioCorta + " - " + s.horaFinCorta + " · " + s.duracionMinutos + " min",
                                style = MaterialTheme.typography.bodyMedium, color = Tinta)
                            Spacer(Modifier.height(4.dp))
                            EtiquetaEstado(s.estado.etiqueta, tipoDe(s.estado))

                            when (s.estado) {
                                EstadoSesion.EN_CURSO -> {
                                    Spacer(Modifier.height(10.dp))
                                    BotonPrincipal("Seguir registrando", onClick = onIniciar)
                                }
                                EstadoSesion.PROGRAMADA -> {
                                    Spacer(Modifier.height(10.dp))
                                    BotonPrincipal("Iniciar sesión de conteo", onClick = { if (vm.iniciar(s.id)) onIniciar() })
                                }
                                else -> {
                                    Spacer(Modifier.height(4.dp))
                                    Text("Ya cerrada: no se puede seguir registrando en ella.",
                                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }

                Seccion("Qué vas a registrar")
                TarjetaSeccion {
                    Text("Clasificación (tránsito o visita) · apariencia · compañía · compras de otra tienda · hacia dónde se dirige, solo si es tránsito.",
                        style = MaterialTheme.typography.bodySmall)
                    Text("Registras una persona por toque, sin escribir y sin identificarla: nunca se guarda nombre ni RUT.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                Nota("Se puede empezar aunque la red falle: la sesión queda y se envía después.")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}