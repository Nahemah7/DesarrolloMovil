package com.example.rinconsur.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.navigation.Ruta
import com.example.rinconsur.ui.theme.Aqua
import com.example.rinconsur.ui.theme.Marino
import com.example.rinconsur.ui.theme.Superficie


data class OpcionMenu(
    val etiqueta: String,
    val ruta: String,
    val icono: ImageVector,
    val iconoActivo: ImageVector
)

private val IconoInactivo = Color(0xFFB7C9D6)
private val SubtituloBarra = Color(0xFFC3D6DF)
private val FondoActivo = Aqua.copy(alpha = 0.24f)
private val FondoVolver = Superficie.copy(alpha = 0.10f)
private val BordeVolver = Superficie.copy(alpha = 0.30f)

fun menuDe(perfil: PerfilUsuario): List<OpcionMenu> = when (perfil) {
    PerfilUsuario.OBSERVADOR -> listOf(
        OpcionMenu("Registrar", Ruta.Registro.ruta, Icons.Outlined.Add, Icons.Filled.Add),
        OpcionMenu("Mis sesiones", Ruta.MiSesion.ruta, Icons.Outlined.DateRange, Icons.Filled.DateRange),
        OpcionMenu("Perfil", Ruta.Perfil.ruta, Icons.Outlined.Person, Icons.Filled.Person)
    )

    // El administrador no mide ni opera: gestiona personas.
    PerfilUsuario.ADMINISTRADOR -> listOf(
        OpcionMenu("Colaboradores", Ruta.Colaboradores.ruta, Icons.Outlined.Person, Icons.Filled.Person),
        OpcionMenu("Perfil", Ruta.Perfil.ruta, Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz)
    )

    // La jefatura lee y decide: entra por el reporte (meta, comparación, horas punta y perfil
    // del tránsito) y desde ahí baja a los indicadores del día.
    PerfilUsuario.JEFE_TIENDA -> listOf(
        OpcionMenu("Reporte", Ruta.Reporte.ruta, Icons.Outlined.Star, Icons.Filled.Star),
        OpcionMenu("Indicadores", Ruta.Indicadores.ruta, Icons.Outlined.BarChart, Icons.Filled.BarChart),
        OpcionMenu("Perfil", Ruta.Perfil.ruta, Icons.Outlined.Person, Icons.Filled.Person)
    )

    // El subgerente responde por el reporte operativo: mira el día y la comparación por mes.
    PerfilUsuario.SUBGERENTE -> listOf(
        OpcionMenu("Indicadores", Ruta.Indicadores.ruta, Icons.Outlined.BarChart, Icons.Filled.BarChart),
        OpcionMenu("Reporte", Ruta.Reporte.ruta, Icons.Outlined.Star, Icons.Filled.Star),
        OpcionMenu("Perfil", Ruta.Perfil.ruta, Icons.Outlined.Person, Icons.Filled.Person)
    )

    PerfilUsuario.SUPERVISOR -> listOf(
        OpcionMenu("Sesiones", Ruta.Sesiones.ruta, Icons.Outlined.List, Icons.Filled.List),
        OpcionMenu("Medir", Ruta.Evento.ruta, Icons.Outlined.Edit, Icons.Filled.Edit),
        OpcionMenu("Indicadores", Ruta.Indicadores.ruta, Icons.Outlined.BarChart, Icons.Filled.BarChart),
        OpcionMenu("Más", Ruta.Perfil.ruta, Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz)
    )
}

private fun esLaActiva(opcion: OpcionMenu, rutaActual: String): Boolean {
    if (opcion.ruta == rutaActual) return true
    return when {
        rutaActual.startsWith("plantilla") || rutaActual.startsWith("cierre") ||
                rutaActual.startsWith("sin_senal") -> opcion.etiqueta == "Registrar"

        rutaActual.startsWith("rechazar") || rutaActual.startsWith("programar") -> opcion.etiqueta == "Sesiones"

        else -> false
    }
}

fun esRaiz(ruta: String): Boolean =
    ruta.startsWith("mi_sesion") || ruta.startsWith("registro") || ruta.startsWith("sesiones") ||
            ruta.startsWith("evento") || ruta.startsWith("perfil") || ruta.startsWith("indicadores") ||
            ruta.startsWith("colaboradores")

@Composable
fun BarraNavegacion(
    titulo: String,
    subtitulo: String,
    opciones: List<OpcionMenu>,
    rutaActual: String,
    onIr: (String) -> Unit,
    onVolver: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(
                Modifier.drawBehind {
                    drawRect(Marino)
                    // El corte diagonal del acceso se repite en la barra, mas discreto por la altura.
                    val ancho = size.width
                    val alto = size.height
                    val cuna = Path().apply {
                        moveTo(ancho * 0.86f, 0f)
                        lineTo(ancho * 0.94f, 0f)
                        lineTo(ancho * 0.70f, alto)
                        lineTo(ancho * 0.62f, alto)
                        close()
                    }
                    drawPath(cuna, Aqua.copy(alpha = 0.16f))
                }
            )
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onVolver != null) {
            BotonBarra(
                icono = Icons.Filled.ArrowBack,
                etiqueta = "Volver",
                color = Superficie,
                fondo = FondoVolver,
                borde = BordeVolver,
                onClick = onVolver
            )
            Spacer(Modifier.width(6.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = Superficie,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (subtitulo.isNotEmpty()) {
                Text(
                    text = subtitulo,
                    color = SubtituloBarra,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.width(6.dp))

        opciones.forEach { opcion ->
            val activa = esLaActiva(opcion, rutaActual)
            BotonBarra(
                icono = if (activa) opcion.iconoActivo else opcion.icono,
                etiqueta = opcion.etiqueta,
                color = if (activa) Superficie else IconoInactivo,
                fondo = if (activa) FondoActivo else Color.Transparent,
                borde = null,
                onClick = { onIr(opcion.ruta) }
            )
        }
    }
}

@Composable
private fun BotonBarra(
    icono: ImageVector,
    etiqueta: String,
    color: Color,
    fondo: Color,
    borde: Color?,
    onClick: () -> Unit
) {
    val forma = RoundedCornerShape(10.dp)
    Box(
        Modifier
            .size(40.dp)
            .background(fondo, forma)
            .then(if (borde != null) Modifier.border(1.dp, borde, forma) else Modifier)
            .clickable(onClick = onClick)
            .semantics { contentDescription = etiqueta },
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
    }
}
