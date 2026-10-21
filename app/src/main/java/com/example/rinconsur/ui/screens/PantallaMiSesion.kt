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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonPrincipal
import com.example.rinconsur.ui.components.NavInferior
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Info
import com.example.rinconsur.ui.theme.Ok
import com.example.rinconsur.ui.theme.Tinta
import com.example.rinconsur.viewmodel.MiSesionViewModel

/** Lo que el observador ve antes de empezar: quien es y que sesion le toca. */
@Composable
fun PantallaMiSesion(onIniciar: () -> Unit, onIrA: (String) -> Unit) {
    val vm: MiSesionViewModel = viewModel()
    val usuario by vm.usuario.collectAsStateWithLifecycle()
    val sesion by vm.sesionAsignada.collectAsStateWithLifecycle()
    val acceso by vm.acceso.collectAsStateWithLifecycle()
    val sinSesion by vm.sinSesionProgramada.collectAsStateWithLifecycle()

    val enCurso = sesion?.estado == EstadoSesion.EN_CURSO

    Scaffold(
        containerColor = Color.White,
        topBar = {
            BarraApp(
                titulo = "Mi sesión",
                subtitulo = usuario?.let { it.nombre + " · " + it.perfil.etiqueta } ?: "",
                etiqueta = if (enCurso) "En curso" else "Programada",
                colorEtiqueta = if (enCurso) Ok else Info
            )
        },
        bottomBar = { NavInferior(listOf("Registrar", "Mis sesiones", "Perfil"), "Mis sesiones", onIrA) }
    ) { pad ->
        Column(Modifier.padding(pad).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            if (sinSesion) {
                Seccion("Hoy")
                TarjetaSeccion(acento = Burdeo) {
                    Text("No tienes una sesión de conteo asignada para hoy.",
                        style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text("Si te toca una, la vas a ver acá cuando el supervisor la programe.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
            } else sesion?.let { s ->
                Seccion("Sesión de hoy")
                TarjetaSeccion(acento = Burdeo) {
                    Text(acceso.ifEmpty { "Acceso sin asignar" }, style = MaterialTheme.typography.titleSmall)
                    Text(s.estado.etiqueta, style = MaterialTheme.typography.bodyMedium, color = if (enCurso) Ok else Info)
                    Text("Hoy " + s.horaInicioCorta + " - " + s.horaFinCorta,
                        style = MaterialTheme.typography.bodyMedium, color = Tinta)
                    Text("Duración " + s.duracionMinutos + " min (debe estar entre 30 y 60)",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                Seccion("Qué vas a registrar")
                TarjetaSeccion {
                    Text("Clasificación (tránsito o visita) · apariencia · compañía · compras de otra tienda · hacia dónde se dirige, solo si es tránsito.",
                        style = MaterialTheme.typography.bodySmall)
                    Text("Registras una persona por toque, sin escribir y sin identificarla: nunca se guarda nombre ni RUT.",
                        style = MaterialTheme.typography.bodySmall, color = Apagado)
                }
                Nota("Se puede empezar aunque la red falle: la sesión queda y se envía después.")
                Spacer(Modifier.height(14.dp))
                BotonPrincipal(
                    if (enCurso) "Seguir registrando" else "Iniciar sesión de conteo",
                    onClick = { if (vm.iniciar() != null) onIniciar() }
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}
