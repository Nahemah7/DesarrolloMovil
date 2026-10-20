package com.example.rinconsur.data

import android.content.Context
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

/**
 * REPOSITORIO
 *
 * Es la unica fuente de datos de la app: los ViewModel le piden a esta clase y nunca hablan con
 * SQLite directamente. Hoy todo sale de la base local; el dia que los ingresos y las boletas
 * lleguen del sistema central de la empresa, se cambia **aca** y ninguna pantalla se toca.
 *
 * Tambien deja el calculo del caso donde corresponde: los porcentajes y el transito estimado los
 * devuelven las vistas de la base, no se recalculan en Kotlin.
 */
class FlujoRepository(context: Context) {

    private val db = FlujoDbHelper(context)

    // ---- catalogos y datos de referencia ----

    fun accesos(): List<Acceso> = db.listarAccesos()

    fun bloques(): List<BloqueHorario> = db.listarBloques()

    fun rangosEtarios(): List<Pair<RangoEtario, String>> = db.listarRangosEtarios()

    fun companias(): List<Compania> = db.listarCompanias()

    /** El dia con el que trabaja la app (la ultima fecha con ingresos cargados). */
    fun fechaDeTrabajo(): String = db.fechaDeTrabajo()

    // ---- personas ----

    fun colaboradorPorRut(rut: String): Colaborador? = db.buscarColaboradorPorRut(rut)

    fun colaboradorPorId(id: Long): Colaborador? = db.buscarColaboradorPorId(id)

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

    fun cerrarSesion(idSesion: Long, hora: String): Int = db.cerrarSesionReal(idSesion, hora)

    /**
     * Validar o rechazar. No borra nada: guarda el estado, quien reviso y el motivo (RN-11).
     */
    fun revisarSesion(idSesion: Long, estado: EstadoSesion, idRevisor: Long, motivo: String? = null): Int =
        db.revisarSesion(idSesion, estado, idRevisor, motivo)

    // ---- registros ----

    /** Cuenta a una persona. Sin texto libre: todo son catalogos, para registrar en menos de 3 s. */
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

    // ---- indicadores (los calcula la base, en sus vistas) ----

    fun indicadoresDeFecha(fecha: String): IndicadoresDia? = db.indicadoresDeFecha(fecha)

    fun transitoPorAcceso(fecha: String): List<TransitoPorAcceso> = db.transitoPorAcceso(fecha)

    fun bloquesPendientes(): List<String> = db.bloquesPendientes()

    // ---- eventos del dia ----

    fun registrarEvento(evento: EventoDia): Long = db.registrarEvento(evento)

    fun eventosDeFecha(fecha: String): List<EventoDia> = db.eventosDeFecha(fecha)

    fun contarEventos(fecha: String): Int = db.contarEventos(fecha)

    fun contarTodosLosEventos(): Int = db.contarTodosLosEventos()

    fun cerrar() = db.close()
}
