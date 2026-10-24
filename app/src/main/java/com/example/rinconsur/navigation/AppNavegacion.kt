package com.example.rinconsur.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.ui.screens.PantallaCierre
import com.example.rinconsur.ui.screens.PantallaEntrada
import com.example.rinconsur.ui.screens.PantallaEvento
import com.example.rinconsur.ui.screens.PantallaLogin
import com.example.rinconsur.ui.screens.PantallaMiSesion
import com.example.rinconsur.ui.screens.PantallaPerfil
import com.example.rinconsur.ui.screens.PantallaProgramar
import com.example.rinconsur.ui.screens.PantallaPlantilla
import com.example.rinconsur.ui.screens.PantallaRechazar
import com.example.rinconsur.ui.screens.PantallaRegistro
import com.example.rinconsur.ui.screens.PantallaSinSenal
import com.example.rinconsur.viewmodel.LoginViewModel

/**
 * Navegacion de la app. El perfil que entra decide el recorrido: el observador registra y cierra su
 * sesion de conteo; el supervisor programa, valida y mira los indicadores.
 *
 * Transicion: al cambiar de pantalla entra desde la derecha y se va hacia la izquierda (y al volver,
 * al reves). 250 ms: se ve de donde viene cada pantalla en vez de un corte seco, y no alcanza a
 * esperar.
 */
@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    val loginViewModel: LoginViewModel = viewModel()
    val duracion = 250

    NavHost(
        navController = navController,
        startDestination = Ruta.Login.ruta,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(duracion)
            ) + fadeIn(animationSpec = tween(duracion))
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Start,
                animationSpec = tween(duracion)
            ) + fadeOut(animationSpec = tween(duracion))
        },
        popEnterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(duracion)
            ) + fadeIn(animationSpec = tween(duracion))
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.End,
                animationSpec = tween(duracion)
            ) + fadeOut(animationSpec = tween(duracion))
        }
    ) {

        composable(Ruta.Login.ruta) {
            PantallaLogin(
                onEntrar = { perfil ->
                    // El observador cuenta; el supervisor programa la sesion y despues la revisa.
                    val destino = if (perfil == PerfilUsuario.OBSERVADOR) Ruta.MiSesion.ruta else Ruta.Programar.ruta
                    navController.navigate(destino) { popUpTo(Ruta.Login.ruta) { inclusive = true } }
                }
            )
        }

        // El supervisor entra a la pantalla interina hasta que existan las suyas.
        composable(Ruta.Entrada.ruta) {
            PantallaEntrada(
                onCerrarSesion = {
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) }
                }
            )
        }

        // ---- Colaborador observador ----
        composable(Ruta.MiSesion.ruta) {
            PantallaMiSesion(
                onIniciar = { navController.navigate(Ruta.Registro.ruta) },
                onIrA = { navController.navegar(it) }
            )
        }
        composable(Ruta.Registro.ruta) {
            PantallaRegistro(
                onPlantilla = { numero -> navController.navigate(rutaDePlantilla(numero)) },
                onCerrar = { navController.navigate(Ruta.Cierre.ruta) },
                onIrA = { navController.navegar(it) }
            )
        }
        composable(Ruta.Plantilla.ruta) { entrada ->
            PantallaPlantilla(
                numero = entrada.arguments?.getString("numero")?.toIntOrNull() ?: 1,
                onVolver = { navController.navigateUp() }
            )
        }
        composable(Ruta.Cierre.ruta) {
            PantallaCierre(
                onEnviar = { navController.navigate(Ruta.SinSenal.ruta) },
                onIrA = { navController.navegar(it) }
            )
        }

        // Estado de la red y sesiones por enviar: se llega despues de cerrar una sesion
        composable(Ruta.SinSenal.ruta) {
            PantallaSinSenal(
                onVolver = { navController.navigate(Ruta.MiSesion.ruta) { popUpTo(Ruta.MiSesion.ruta) } }
            )
        }

        // ---- Comunes ----
        // ---- Supervisor de piso ----
        composable(Ruta.Programar.ruta) { PantallaProgramar(onIrA = { navController.navegar(it) }) }
        composable(Ruta.Evento.ruta) { PantallaEvento(onVolver = { navController.navigateUp() }) }
        composable(Ruta.Rechazar.ruta) { entrada ->
            PantallaRechazar(
                idSesion = entrada.arguments?.getString("idSesion")?.toLongOrNull() ?: 0L,
                onVolver = { navController.navigateUp() }
            )
        }

        composable(Ruta.Perfil.ruta) {
            PantallaPerfil(
                onSalir = {
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) }
                }
            )
        }
    }
}

/** La pestana de abajo se traduce a su ruta. Las del supervisor llegan con sus pantallas. */
private fun NavHostController.navegar(pestana: String) {
    val destino = when (pestana) {
        "Registrar" -> Ruta.Registro.ruta
        "Mis sesiones" -> Ruta.MiSesion.ruta
        "Sesiones" -> Ruta.Programar.ruta
        "Medir" -> Ruta.Evento.ruta
        else -> Ruta.Perfil.ruta
    }
    navigate(destino) { launchSingleTop = true }
}