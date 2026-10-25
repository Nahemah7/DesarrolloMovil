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
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.navigation.Ruta
import com.example.rinconsur.ui.theme.Burdeo
import com.example.rinconsur.ui.theme.Hueso

/**
 * Barra de navegacion de la app: el titulo de la pantalla, la accion de volver y el menu del perfil.
 *
 * Vive en el contenedor (AppNavegacion), no en las pantallas: por eso el menu se ve igual en todas y
 * no se pierde al cambiar de pantalla. Los iconos van sin nombre; cada uno lleva su descripcion para
 * el lector de pantalla, y el activo se marca con fondo y con el icono relleno, no solo con color.
 */

/** Una opcion del menu: su nombre para el lector de pantalla, su ruta y sus dos iconos (normal y activo). */
data class OpcionMenu(
    val etiqueta: String,
    val ruta: String,
    val icono: ImageVector,
    val iconoActivo: ImageVector
)

/** Colores de la barra sobre burdeo. El gris apagado de la app no sirve aca: no alcanza el contraste. */
private val IconoInactivo = Color(0xFFD8C3CA)
private val SubtituloBarra = Color(0xFFE9D9DE)
private val FondoActivo = Hueso.copy(alpha = 0.20f)
private val FondoVolver = Hueso.copy(alpha = 0.10f)
private val BordeVolver = Hueso.copy(alpha = 0.30f)

/** Las opciones de cada perfil: el observador tiene tres, el supervisor cuatro. */
fun menuDe(perfil: PerfilUsuario): List<OpcionMenu> = when (perfil) {
    PerfilUsuario.OBSERVADOR -> listOf(
        OpcionMenu("Registrar", Ruta.Registro.ruta, Icons.Outlined.Add, Icons.Filled.Add),
        OpcionMenu("Mis sesiones", Ruta.MiSesion.ruta, Icons.Outlined.DateRange, Icons.Filled.DateRange),
        OpcionMenu("Perfil", Ruta.Perfil.ruta, Icons.Outlined.Person, Icons.Filled.Person)
    )

    // La pantalla de indicadores todavia no existe; su icono entra a Perfil, que es donde caia
    // antes desde la barra inferior, y se conecta a su ruta cuando la pantalla exista.
    else -> listOf(
        OpcionMenu("Sesiones", Ruta.Programar.ruta, Icons.Outlined.List, Icons.Filled.List),
        OpcionMenu("Medir", Ruta.Evento.ruta, Icons.Outlined.Edit, Icons.Filled.Edit),
        OpcionMenu("Indicadores", Ruta.Perfil.ruta, Icons.Outlined.BarChart, Icons.Filled.BarChart),
        OpcionMenu("Más", Ruta.Perfil.ruta, Icons.Outlined.MoreHoriz, Icons.Filled.MoreHoriz)
    )
}

/**
 * Cada icono del menu es un destino propio y su pantalla queda marcada: no se marca la opcion de
 * donde se venia. Lo unico que se hereda es la hoja o el paso que cuelga de una pantalla con icono
 * (la plantilla se abre desde Registrar, el rechazo se hace sobre una sesion de Sesiones).
 */
private fun esLaActiva(opcion: OpcionMenu, rutaActual: String): Boolean {
    if (opcion.ruta == rutaActual) return true
    return when {
        // Pasos del registro del observador: la hoja de plantilla y el cierre de su sesion.
        rutaActual.startsWith("plantilla") || rutaActual.startsWith("cierre") ||
            rutaActual.startsWith("sin_senal") -> opcion.etiqueta == "Registrar"

        // El rechazo se hace sobre una sesion, que es lo que muestra Sesiones.
        rutaActual.startsWith("rechazar") -> opcion.etiqueta == "Sesiones"

        else -> false
    }
}

/**
 * Las pantallas que son destino de un icono: no llevan flecha, porque el menu ya es la forma de
 * moverse y volver atras no corresponde a ninguna de ellas.
 */
fun esRaiz(ruta: String): Boolean =
    ruta.startsWith("mi_sesion") || ruta.startsWith("registro") || ruta.startsWith("programar") ||
        ruta.startsWith("evento") || ruta.startsWith("perfil")

@Composable
fun BarraNavegacion(
    titulo: String,
    subtitulo: String,
    opciones: List<OpcionMenu>,
    rutaActual: String,
    onIr: (String) -> Unit,
    /** Solo en pantallas de flujo: en las raices no se dibuja la flecha. */
    onVolver: (() -> Unit)? = null
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(Burdeo)
            .statusBarsPadding()
            .heightIn(min = 56.dp)
            .padding(start = 10.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (onVolver != null) {
            BotonBarra(
                icono = Icons.Filled.ArrowBack,
                etiqueta = "Volver",
                color = Hueso,
                fondo = FondoVolver,
                borde = BordeVolver,
                onClick = onVolver
            )
            Spacer(Modifier.width(6.dp))
        }

        Column(Modifier.weight(1f)) {
            Text(
                text = titulo,
                color = Hueso,
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
                color = if (activa) Hueso else IconoInactivo,
                fondo = if (activa) FondoActivo else Color.Transparent,
                borde = null,
                onClick = { onIr(opcion.ruta) }
            )
        }
    }
}

/** Un icono de la barra: area tactil de 40 x 40 dp con su descripcion para el lector de pantalla. */
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
