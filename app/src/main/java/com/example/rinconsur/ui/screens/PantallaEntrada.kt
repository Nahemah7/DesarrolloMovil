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
import com.example.rinconsur.ui.components.BarraApp
import com.example.rinconsur.ui.components.BotonSecundario
import com.example.rinconsur.ui.components.Nota
import com.example.rinconsur.ui.components.Seccion
import com.example.rinconsur.ui.components.TarjetaSeccion
import com.example.rinconsur.ui.theme.Apagado
import com.example.rinconsur.ui.theme.Dimens
import com.example.rinconsur.viewmodel.EntradaViewModel

/**
 * Sesion iniciada. Muestra quien entro y los accesos de la tienda, que vienen de la base local:
 * es la prueba de que el login y los datos funcionan de punta a punta.
 *
 * Es una pantalla interina: el observador y el supervisor entran a sus propias pantallas de conteo,
 * que reemplazan a esta cuando esten listas.
 */
@Composable
fun PantallaEntrada(onCerrarSesion: () -> Unit) {
    val vm: EntradaViewModel = viewModel()
    val estado by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = Color.White,
        topBar = {
            BarraApp(
                titulo = "Rincon Sur",
                subtitulo = estado.perfil.etiqueta,
                etiqueta = "Sesion iniciada",
                colorEtiqueta = com.example.rinconsur.ui.theme.Info
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Dimens.espacioPantalla)
        ) {
            Seccion("Quien usa el telefono")
            TarjetaSeccion {
                Text(estado.nombre, style = MaterialTheme.typography.titleSmall)
                Text(
                    estado.perfil.etiqueta + " · " + estado.rut,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Apagado
                )
                Nota("El telefono se comparte entre turnos: al cerrar sesion se borra quien estaba adentro.")
            }

            Seccion("Accesos de la tienda")
            TarjetaSeccion {
                estado.accesos.forEach { acceso ->
                    Text(acceso.etiquetaCorta, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(Dimens.espacioMedio))
            BotonSecundario(texto = "Cerrar sesion", onClick = onCerrarSesion)
            Spacer(Modifier.height(36.dp))
        }
    }
}
