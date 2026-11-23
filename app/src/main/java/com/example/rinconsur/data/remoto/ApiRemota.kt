package com.example.rinconsur.data.remoto

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

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

    @POST("api/sesiones")
    suspend fun programar(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Body peticion: PeticionSesionNueva
    ): Response<SesionRemota>

    @GET("api/indicadores/supervisores")
    suspend fun supervisores(
        @retrofit2.http.Header("Authorization") autorizacion: String
    ): Response<List<SupervisorRemoto>>

    @GET("api/indicadores/personal")
    suspend fun personal(
        @retrofit2.http.Header("Authorization") autorizacion: String
    ): Response<List<PersonalRemoto>>

    @retrofit2.http.PATCH("api/sesiones/{id}")
    suspend fun editar(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("id") idSesion: Int,
        @Body peticion: PeticionEditarSesion
    ): Response<SesionRemota>

    @retrofit2.http.PATCH("api/sesiones/{id}/iniciar")
    suspend fun iniciar(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("id") idSesion: Int,
        @Body cuerpo: Map<String, String> = emptyMap()
    ): Response<SesionRemota>

    @POST("api/registros")
    suspend fun registrar(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Body peticion: PeticionRegistro
    ): Response<RegistroRemoto>

    @GET("api/sesiones/por-validar")
    suspend fun porValidar(
        @retrofit2.http.Header("Authorization") autorizacion: String
    ): Response<List<SesionRemota>>

    @retrofit2.http.PATCH("api/sesiones/{id}/validar")
    suspend fun validar(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("id") idSesion: Int,
        @Body cuerpo: Map<String, String> = emptyMap()
    ): Response<SesionRemota>

    @GET("api/indicadores/reporte/{fecha}")
    suspend fun reporte(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("fecha") fecha: String
    ): Response<ReporteRemoto>

    @GET("api/indicadores/{fecha}/accesos")
    suspend fun accesosDelDia(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("fecha") fecha: String
    ): Response<List<AccesoMedidoRemoto>>

    @GET("api/indicadores/accesos")
    suspend fun accesosDelTipo(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @retrofit2.http.Query("tipoDia") tipoDia: String,
        @retrofit2.http.Query("desde") desde: String?,
        @retrofit2.http.Query("hasta") hasta: String?
    ): Response<List<AccesoMedidoRemoto>>

    @GET("api/indicadores")
    suspend fun indicadoresDelTipo(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @retrofit2.http.Query("tipoDia") tipoDia: String,
        @retrofit2.http.Query("desde") desde: String?,
        @retrofit2.http.Query("hasta") hasta: String?
    ): Response<IndicadorRemoto>

    @GET("api/indicadores/{fecha}")
    suspend fun indicadorDelDia(
        @retrofit2.http.Header("Authorization") autorizacion: String,
        @Path("fecha") fecha: String
    ): Response<IndicadorRemoto>
}

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

data class PeticionEditarSesion(
    val idAcceso: Int,
    val horaInicio: String,
    val horaFin: String,
    val idObservador: Int,
    val observadorNombre: String?
)

data class SupervisorRemoto(
    val idSupervisor: Int?,
    val programadas: Long?,
    val pendientes: Long?,
    val validadas: Long?,
    val rechazadas: Long?
)

data class PersonalRemoto(
    val idObservador: Int?,
    val observador: String?,
    val sesiones: Long?,
    val cerradas: Long?,
    val validadas: Long?,
    val rechazadas: Long?,
    val registros: Long?,
    val transito: Long?,
    val ultimaFecha: String?
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
    val idObservador: Int?,
    val inicioReal: String?,
    val finReal: String?,
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

data class AccesoMedidoRemoto(
    val idAcceso: Int = 0,
    val ingresos: Long = 0,
    val transito: Long? = null,
    val origen: String = ""
)

data class PeticionRegistro(
    val idSesion: Int,
    val idClasificacion: Int,
    val idRango: Int,
    val idTipoCompania: Int,
    val comprasOtraTienda: Boolean,
    val idAccesoDestino: Int?,
    val fechaHora: String?
)

data class RegistroRemoto(
    val id: Int?,
    val idSesion: Int?,
    val fechaHora: String?,
    val anulado: Boolean?
)

data class PeticionSesionNueva(
    val idAcceso: Int,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String,
    val idObservador: Int,
    val observadorNombre: String?
)
