package com.example.rinconsur.data.remoto

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class ClienteRemoto(direccion: String) {

    val autenticacion: ApiAutenticacion
    val catalogos: ApiCatalogos
    val medicion: ApiMedicion

    init {
        val base = normalizar(direccion)
        val registro = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        val http = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(registro)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(base)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        autenticacion = retrofit.create(ApiAutenticacion::class.java)
        catalogos = retrofit.create(ApiCatalogos::class.java)
        medicion = retrofit.create(ApiMedicion::class.java)
    }

    companion object {

        fun normalizar(direccion: String): String {
            val limpia = direccion.trim().ifBlank { "http://localhost:8090" }
            val conEsquema = if (limpia.startsWith("http")) limpia else "http://" + limpia
            return if (conEsquema.endsWith("/")) conEsquema else conEsquema + "/"
        }
    }
}
