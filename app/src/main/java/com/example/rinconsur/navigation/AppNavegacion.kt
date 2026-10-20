package com.example.rinconsur.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rinconsur.ui.screens.PantallaEntrada
import com.example.rinconsur.ui.screens.PantallaLogin
import com.example.rinconsur.viewmodel.LoginViewModel

/**
 * Navegacion de la app. El perfil que entra decide el recorrido: el observador registra y cierra su
 * sesion; el supervisor programa, valida y mira los indicadores. Por ahora el recorrido llega hasta
 * la pantalla de sesion iniciada.
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
            PantallaLogin(onEntrar = { navController.navigate(Ruta.Entrada.ruta) })
        }
        composable(Ruta.Entrada.ruta) {
            PantallaEntrada(
                onCerrarSesion = {
                    loginViewModel.cerrarSesion()
                    navController.navigate(Ruta.Login.ruta) { popUpTo(0) }
                }
            )
        }
    }
}
