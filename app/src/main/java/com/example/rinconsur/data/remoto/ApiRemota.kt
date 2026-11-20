package com.example.rinconsur.data.remoto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Las tres interfaces del cliente, tal como las expone el gateway. Los nombres de campo son los
// del JSON del backend, así que no hay traducciones escondidas entre la app y los servicios.
interface ApiAutenticacion {

    @POST("api/auth/login")
    suspend fun entrar(@Body peticion: PeticionLogin): Response<RespuestaLogin>
}

interface ApiCatalogos {

    @GET("api/catalogos/accesos")
    suspend fun accesos(@retrofit2.http.Header("Authorization") autorizacion: String): Response<List<AccesoRemoto>>

    @GET("api/catalogos/bloques")
    suspend fun bloques(@retrofit2.http.Header("Authorization") autorizacion: String): Response<List<BloqueRemoto>>
}

interface ApiMedicion {

    @GET("api/sesiones")
    suspend fun sesionesDelDia(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @retrofit2.http.Query("fecha") fecha: String
    ): Response<List<SesionRemota>>

    @GET("api/sesiones/por-validar")
    suspend fun porValidar(
        @retrofit2.http.Header("Authorization") autorizacion: String
    ): Response<List<SesionRemota>>

    // Retrofit exige un cuerpo en PATCH: esta llamada manda un objeto vacío que el servicio
    // ignora, porque el revisor sale del token. Sin el cuerpo, la petición falla en el cliente
    // y la app se cae al respaldo local sin que se note.
    @retrofit2.http.PATCH("api/sesiones/{id}/validar")
    suspend fun validar(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("id") idSesion: Int,
        @Body cuerpo: Map<String, String> = emptyMap()
    ): Response<SesionRemota>

    @GET("api/indicadores/{fecha}")
    suspend fun indicadorDelDia(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("fecha") fecha: String
    ): Response<IndicadorRemoto>
}

// ---------- cuerpos ----------

data class PeticionLogin(val rut: String, val clave: String)

data class RespuestaLogin(val token: String?, val usuario: UsuarioRemoto?)

data class UsuarioRemoto(
    val id: Int?,
    val rut: String?,
    val nombre: String?,
    val cargo: String?,
    val rol: String?,
    val correo: String?,
    val telefono: String?,
    val genero: String?,
    val activo: Boolean?,
    val debeCambiarClave: Boolean?
)

data class AccesoRemoto(
    val id: Int?,
    val codigo: String?,
    val nombre: String?,
    val ubicacion: String?,
    val observacion: String?
)

data class BloqueRemoto(
    val id: Int?,
    val horaInicio: String?,
    val horaFin: String?
)

data class SesionRemota(
    val id: Int?,
    val codigo: String?,
    val idAcceso: Int?,
    val idBloque: Int?,
    val fecha: String?,
    val horaInicio: String?,
    val horaFin: String?,
    val estado: String?,
    val observador: String?,
    val registros: Long?
)

data class IndicadorRemoto(
    val desde: String?,
    val hasta: String?,
    val tipoDia: String?,
    val ingresos: Long?,
    val transacciones: Long?,
    val conversionBruta: Double?,
    val dia: String?,
    val transito: Double?,
    val medidos: Int?,
    val sinInformacion: Int?
)
