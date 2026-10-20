package com.example.rinconsur.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

/**
 * Recurso nativo del dispositivo: el estado de la red (ConnectivityManager).
 *
 * Existe por una regla del caso: si la red falla, la app no puede inventar ni perder datos
 * (RNF-03). El telefono siempre guarda en la base local; lo que la red decide es **si la
 * sesion se puede enviar**. Antes de decir "enviado" hay que preguntarle al sistema, no
 * suponerlo.
 *
 * Permiso: ACCESS_NETWORK_STATE (es de nivel normal, no se pide en pantalla).
 */
class EstadoRed(context: Context) {

    private val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    /** Hay internet de verdad: red activa y con capacidad de salir. */
    fun hayInternet(): Boolean {
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    /** Como se esta conectando: wifi, datos moviles o sin conexion. Para mostrarlo en pantalla. */
    fun medioDeConexion(): MedioConexion {
        val red = gestor.activeNetwork ?: return MedioConexion.SIN_CONEXION
        val capacidades = gestor.getNetworkCapabilities(red) ?: return MedioConexion.SIN_CONEXION
        return when {
            capacidades.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> MedioConexion.WIFI
            capacidades.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> MedioConexion.DATOS_MOVILES
            capacidades.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> MedioConexion.CABLE
            else -> MedioConexion.SIN_CONEXION
        }
    }
}

enum class MedioConexion(val etiqueta: String) {
    WIFI("Wi-Fi"),
    DATOS_MOVILES("Datos moviles"),
    CABLE("Cable"),
    SIN_CONEXION("Sin conexion")
}
