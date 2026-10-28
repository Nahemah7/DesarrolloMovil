package com.example.rinconsur.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class EstadoRed(context: Context) {

    private val gestor = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    fun hayInternet(): Boolean {
        val red = gestor.activeNetwork ?: return false
        val capacidades = gestor.getNetworkCapabilities(red) ?: return false
        return capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capacidades.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

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
