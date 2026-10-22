package com.example.rinconsur.data

import android.content.Context
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.RangoEtario

/** Una plantilla de prerellenado: lo que el observador repite cuando entra alguien del mismo perfil. */
data class Plantilla(
    val clasificacion: Clasificacion,
    val rangoEtario: RangoEtario,
    val compania: Compania,
    val comprasOtraTienda: Boolean,
    val destino: Long?
)

/**
 * Las tres plantillas del observador, guardadas en las preferencias del telefono.
 *
 * No van a la base: son una ayuda para registrar mas rapido, no un dato de la sesion, y no se envian
 * a validacion. Cada una se guarda como una linea de texto con los cinco valores.
 */
class Plantillas(context: Context) {

    private val prefs = context.getSharedPreferences("plantillas", Context.MODE_PRIVATE)

    fun leer(numero: Int): Plantilla? {
        val guardado = prefs.getString(clave(numero), null) ?: return null
        val p = guardado.split("|")
        if (p.size < 5) return null
        val clasificacion = Clasificacion.entries.firstOrNull { it.name == p[0] } ?: return null
        val rango = RangoEtario.entries.firstOrNull { it.name == p[1] } ?: return null
        val compania = Compania.entries.firstOrNull { it.name == p[2] } ?: return null
        return Plantilla(
            clasificacion = clasificacion,
            rangoEtario = rango,
            compania = compania,
            comprasOtraTienda = p[3] == "1",
            destino = p[4].takeIf { it != "-" }?.toLongOrNull()
        )
    }

    fun guardar(numero: Int, plantilla: Plantilla) {
        val linea = listOf(
            plantilla.clasificacion.name,
            plantilla.rangoEtario.name,
            plantilla.compania.name,
            if (plantilla.comprasOtraTienda) "1" else "0",
            plantilla.destino?.toString() ?: "-"
        ).joinToString("|")
        prefs.edit().putString(clave(numero), linea).apply()
    }

    fun borrar(numero: Int) {
        prefs.edit().remove(clave(numero)).apply()
    }

    private fun clave(numero: Int) = "plantilla_$numero"
}
