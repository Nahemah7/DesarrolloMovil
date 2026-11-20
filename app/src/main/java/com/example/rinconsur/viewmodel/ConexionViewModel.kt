package com.example.rinconsur.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.rinconsur.data.remoto.ClienteRemoto
import com.example.rinconsur.data.remoto.DireccionServidor
import com.example.rinconsur.data.remoto.PeticionLogin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Una prueba por paso, con su tiempo: sirve para ver de un vistazo si el problema es la red, el
// gateway o un servicio, en vez de "no carga".
data class PasoDeConexion(
    val nombre: String,
    val detalle: String = "",
    val milisegundos: Long = 0,
    val estado: EstadoPaso = EstadoPaso.PENDIENTE
)

enum class EstadoPaso { PENDIENTE, OK, ERROR }

data class ConexionUiState(
    val direccion: String = "",
    val probando: Boolean = false,
    val pasos: List<PasoDeConexion> = vacio(),
    val mensaje: String? = null
) {
    val todoBien: Boolean get() = pasos.isNotEmpty() && pasos.all { it.estado == EstadoPaso.OK }
}

private fun vacio() = listOf(
    PasoDeConexion("Inicio de sesión"),
    PasoDeConexion("Catálogos"),
    PasoDeConexion("Sesiones del 15-09"),
    PasoDeConexion("Indicadores del 15-09")
)

class ConexionViewModel(application: Application) : AndroidViewModel(application) {

    private val guardado = DireccionServidor(application)
    private val _uiState = MutableStateFlow(ConexionUiState(direccion = guardado.leer()))
    val uiState: StateFlow<ConexionUiState> = _uiState.asStateFlow()

    fun onDireccion(cambio: String) {
        _uiState.value = _uiState.value.copy(direccion = cambio, mensaje = null)
    }

    fun probar() {
        val direccion = ClienteRemoto.normalizar(_uiState.value.direccion)
        guardado.guardar(direccion)
        _uiState.value = _uiState.value.copy(direccion = direccion, probando = true, mensaje = null,
            pasos = vacio())

        viewModelScope.launch {
            val cliente = ClienteRemoto(direccion)

            // 1) login con una cuenta sembrada
            val llamadaLogin = medir { cliente.autenticacion.entrar(PeticionLogin(RUT, CLAVE)) }
            val login = llamadaLogin.valor
            val msLogin = llamadaLogin.milisegundos
            val cuerpo = login?.body()
            val token = cuerpo?.token
            if (login == null || !login.isSuccessful || token.isNullOrBlank()) {
                marcar(0, "Inicio de sesión", EstadoPaso.ERROR,
                    motivo(login?.code(), login?.errorBody()?.string(), llamadaLogin.fallo), msLogin)
                listo("No se pudo iniciar sesión: revisa que el backend esté arriba y la dirección sea la del PC.")
                return@launch
            }
            actualizar(0, { it.copy(estado = EstadoPaso.OK, milisegundos = msLogin,
                detalle = "${cuerpo.usuario?.nombre} · ${cuerpo.usuario?.rol} · token de ${token.length} caracteres") })

            val autorizacion = "Bearer " + token

            // 2) catálogos
            val llamadaCat = medir { cliente.catalogos.accesos(autorizacion) }
            val catalogos = llamadaCat.valor
            val msCat = llamadaCat.milisegundos
            val listaAccesos = catalogos?.body()
            if (catalogos == null || !catalogos.isSuccessful || listaAccesos == null) {
                marcar(1, "Catálogos", EstadoPaso.ERROR,
                    motivo(catalogos?.code(), catalogos?.errorBody()?.string(), llamadaCat.fallo), msCat)
            } else {
                actualizar(1, { it.copy(estado = EstadoPaso.OK, milisegundos = msCat,
                    detalle = listaAccesos.size.toString() + " accesos · " + listaAccesos.joinToString(", ") { a -> a.codigo ?: "" }) })
            }

            // 3) sesiones de un día con datos del anexo
            val llamadaSes = medir { cliente.medicion.sesionesDelDia(autorizacion, FECHA) }
            val sesiones = llamadaSes.valor
            val msSes = llamadaSes.milisegundos
            val listaSesiones = sesiones?.body()
            if (sesiones == null || !sesiones.isSuccessful || listaSesiones == null) {
                marcar(2, "Sesiones del 15-09", EstadoPaso.ERROR,
                    motivo(sesiones?.code(), sesiones?.errorBody()?.string(), llamadaSes.fallo), msSes)
            } else {
                actualizar(2, { it.copy(estado = EstadoPaso.OK, milisegundos = msSes,
                    detalle = listaSesiones.size.toString() + " sesiones · " +
                        listaSesiones.joinToString(" · ") { s -> (s.codigo ?: "") + " " + (s.estado ?: "") }) })
            }

            // 4) indicadores del mismo día
            val llamadaInd = medir { cliente.medicion.indicadorDelDia(autorizacion, FECHA) }
            val indicador = llamadaInd.valor
            val msInd = llamadaInd.milisegundos
            val cuerpoInd = indicador?.body()
            if (indicador == null || !indicador.isSuccessful || cuerpoInd == null) {
                marcar(3, "Indicadores del 15-09", EstadoPaso.ERROR,
                    motivo(indicador?.code(), indicador?.errorBody()?.string(), llamadaInd.fallo), msInd)
            } else {
                actualizar(3, { it.copy(estado = EstadoPaso.OK, milisegundos = msInd,
                    detalle = "${cuerpoInd.ingresos} ingresos · ${cuerpoInd.transacciones} transacciones · " +
                        "${cuerpoInd.conversionBruta} % de conversión") })
            }

            val todos = _uiState.value.pasos.all { it.estado == EstadoPaso.OK }
            listo(if (todos) "Conexión completa: la app habló con el gateway y con los tres servicios."
            else "Algún paso falló. El detalle está en cada fila.")
        }
    }

    // Cronometra la llamada y conserva el motivo del fallo: sin el mensaje de la excepción,
    // "no hubo respuesta" no dice nada (¿red, permiso, servidor apagado?).
    private suspend fun <T> medir(bloque: suspend () -> T): ResultadoLlamada<T> =
        withContext(Dispatchers.IO) {
            val inicio = System.currentTimeMillis()
            val intento = runCatching { bloque() }
            ResultadoLlamada(intento.getOrNull(), System.currentTimeMillis() - inicio,
                intento.exceptionOrNull()?.let { it.message ?: it::class.java.simpleName })
        }

    private data class ResultadoLlamada<T>(val valor: T?, val milisegundos: Long, val fallo: String?)

    private fun actualizar(indice: Int, cambio: (PasoDeConexion) -> PasoDeConexion) {
        val pasos = _uiState.value.pasos.toMutableList()
        pasos[indice] = cambio(pasos[indice])
        _uiState.value = _uiState.value.copy(pasos = pasos)
    }

    private fun marcar(indice: Int, nombre: String, estado: EstadoPaso, detalle: String?, ms: Long) {
        actualizar(indice) { it.copy(nombre = nombre, estado = estado, detalle = detalle ?: "sin respuesta", milisegundos = ms) }
    }

    private fun listo(mensaje: String) {
        _uiState.value = _uiState.value.copy(probando = false, mensaje = mensaje)
    }

    private fun motivo(codigo: Int?, cuerpo: String?, fallo: String? = null): String {
        if (codigo == null) {
            return "No hubo respuesta: " + (fallo ?: "sin detalle")
        }
        val texto = cuerpo?.let { runCatching { org.json.JSONObject(it).optString("motivo") }.getOrNull() }
        return "HTTP $codigo" + if (!texto.isNullOrBlank()) " · $texto" else ""
    }

    private companion object {
        // Una cuenta sembrada del caso, solo para la prueba de conexión.
        const val RUT = "15.234.871-1"
        const val CLAVE = "rincon2026"
        const val FECHA = "2026-09-15"
    }
}
