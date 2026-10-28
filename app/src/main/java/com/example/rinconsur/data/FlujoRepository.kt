package com.example.rinconsur.data

import android.content.Context
import com.example.rinconsur.model.PreguntaSeguridad
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.BloqueHorario
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.EventoDia
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.Registro
import com.example.rinconsur.model.ResumenSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.model.TransitoPorAcceso

class FlujoRepository(context: Context) {

    private val db = FlujoDbHelper(context)

    companion object {
        const val MINIMO_REGISTROS = FlujoDbHelper.MINIMO_REGISTROS
    }

    // ---- catalogos y datos de referencia ----

    fun accesos(): List<Acceso> = db.listarAccesos()

    fun bloques(): List<BloqueHorario> = db.listarBloques()

    fun rangosEtarios(): List<Pair<RangoEtario, String>> = db.listarRangosEtarios()

    fun companias(): List<Compania> = db.listarCompanias()

    fun fechaHoy(): String = db.fechaHoy()

    fun fechaDeTrabajo(): String = db.fechaDeTrabajo()

    // ---- personas ----

    fun colaboradorPorRut(rut: String): Colaborador? = db.buscarColaboradorPorRut(rut)

    fun colaboradorPorId(id: Long): Colaborador? = db.buscarColaboradorPorId(id)

    fun cambiarNombre(idUsuario: Long, nombres: String, apellidoPaterno: String): Boolean =
        db.cambiarNombre(idUsuario, nombres, apellidoPaterno) > 0

    // ---- sesiones de conteo ----

    fun sesionEnCursoDe(idObservador: Long): SesionResumen? = db.sesionEnCursoDe(idObservador)

    fun sesionesDeFecha(fecha: String): List<SesionResumen> = db.sesionesDeFecha(fecha)

    fun sesion(idSesion: Long): SesionResumen? = db.buscarSesion(idSesion)

    fun sesionesPorValidar(): List<SesionResumen> = db.sesionesPorValidarTodas()

    fun sesionesPorValidar(fecha: String): List<SesionResumen> = db.sesionesPorValidar(fecha)

    fun sesionesDeLosUltimosDias(desde: String, dias: Int): List<SesionResumen> =
        db.sesionesDeLosUltimosDias(desde, dias)

    fun resumenDeSesion(idSesion: Long): ResumenSesion = db.resumenDeSesion(idSesion)

    fun iniciarSesion(idSesion: Long, hora: String): Int = db.iniciarSesion(idSesion, hora)
    fun observadores(): List<Colaborador> = db.listarObservadores()

    fun programarSesion(
        idAcceso: Long,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        idObservador: Long,
        idProgramador: Long
    ): String = db.programarSesion(idAcceso, fecha, horaInicio, horaFin, idObservador, idProgramador)

    fun cerrarSesion(idSesion: Long, hora: String): Int = db.cerrarSesionReal(idSesion, hora)

    fun revisarSesion(idSesion: Long, estado: EstadoSesion, idRevisor: Long, motivo: String? = null): Int =
        db.revisarSesion(idSesion, estado, idRevisor, motivo)

    // ---- registros ----

    fun registrar(
        idSesion: Long,
        clasificacion: Clasificacion,
        rangoEtario: RangoEtario,
        compania: Compania,
        comprasOtraTienda: Boolean,
        seDirige: Long? = null
    ): Long = db.insertarRegistro(idSesion, clasificacion, rangoEtario, compania, comprasOtraTienda, seDirige)

    fun anularRegistro(idRegistro: Long, motivo: String, idColaborador: Long): Int =
        db.anularRegistro(idRegistro, motivo, idColaborador)

    fun ultimosRegistros(idSesion: Long, limite: Int = 8): List<Registro> =
        db.ultimosRegistros(idSesion, limite)

    fun indicadoresDeFecha(fecha: String): IndicadoresDia? = db.indicadoresDeFecha(fecha)

    fun indicadoresDeTipoDia(tipo: String, desde: String? = null, hasta: String? = null): IndicadoresDia? =
        db.indicadoresDeTipoDia(tipo, desde, hasta)

    fun transitoPorAccesoDeTipo(tipo: String, desde: String? = null, hasta: String? = null): List<TransitoPorAcceso> =
        db.transitoPorAccesoDeTipo(tipo, desde, hasta)

    fun semanasConDatos(): List<Pair<String, String>> = db.semanasConDatos()


    fun transitoPorAcceso(fecha: String): List<TransitoPorAcceso> = db.transitoPorAcceso(fecha)

    fun bloquesPendientes(fecha: String): List<String> = db.bloquesPendientes(fecha)

    // ---- eventos del dia ----

    fun registrarEvento(evento: EventoDia): Long = db.registrarEvento(evento)

    fun eventosRegistrados(): List<EventoDia> = db.eventosRegistrados()

    fun anularEvento(idEvento: Long, motivo: String, idColaborador: Long): Int =
        db.anularEvento(idEvento, motivo, idColaborador)

    fun contarEventos(fecha: String): Int = db.contarEventos(fecha)

    fun contarTodosLosEventos(): Int = db.contarTodosLosEventos()

    fun preguntas(): List<PreguntaSeguridad> = db.listarPreguntas()
    fun preguntasDe(idUsuario: Long): List<PreguntaSeguridad> = db.listarPreguntasDe(idUsuario)
    fun tienePreguntas(idUsuario: Long): Boolean = db.tienePreguntas(idUsuario)
    fun guardarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>) =
        db.guardarRespuestas(idUsuario, rut, respuestas)
    fun verificarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>): Boolean =
        db.verificarRespuestas(idUsuario, rut, respuestas)
    fun cambiarClave(idUsuario: Long, nuevoHash: String): Boolean = db.cambiarClave(idUsuario, nuevoHash)

    fun fotoDe(idUsuario: Long): ByteArray? = db.fotoDe(idUsuario)

    fun guardarFoto(idUsuario: Long, foto: ByteArray?): Boolean = db.guardarFoto(idUsuario, foto)

    fun cerrar() = db.close()
}
