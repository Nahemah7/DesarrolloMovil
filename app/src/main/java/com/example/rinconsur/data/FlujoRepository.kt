package com.example.rinconsur.data

import android.content.Context
import com.example.rinconsur.model.MedicionPeriodo
import com.example.rinconsur.model.RegistroPendiente
import com.example.rinconsur.model.NuevaPersona
import com.example.rinconsur.model.PerfilUsuario
import com.example.rinconsur.model.PreguntaSeguridad
import com.example.rinconsur.model.Acceso
import com.example.rinconsur.model.BloqueHorario
import com.example.rinconsur.model.Clasificacion
import com.example.rinconsur.model.CargaDia
import com.example.rinconsur.model.Genero
import com.example.rinconsur.model.Colaborador
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.EstadoSesion
import com.example.rinconsur.model.EventoDia
import com.example.rinconsur.model.IndicadoresDia
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.Registro
import com.example.rinconsur.model.ResumenSesion
import com.example.rinconsur.model.DetalleSesion
import com.example.rinconsur.model.SesionResumen
import com.example.rinconsur.model.ReporteJefatura
import com.example.rinconsur.model.TransitoPorAcceso

class FlujoRepository(context: Context) {

    private val db = FlujoDbHelper(context)

    private val catalogoDao = CatalogoDao(db)
    private val usuarioDao = UsuarioDao(db)
    private val sesionDao = SesionDao(db)
    private val indicadorDao = IndicadorDao(db)
    private val eventoDao = EventoDao(db)
    private val cargaDao = CargaDao(db)
    private val mantenimientoDao = MantenimientoDao(db)
    private val colaDao = ColaDao(db)

    companion object {
        const val MINIMO_REGISTROS = FlujoDbHelper.MINIMO_REGISTROS
    }

    // ---- catalogos y datos de referencia ----

    fun accesos(): List<Acceso> = catalogoDao.listarAccesos()

    fun bloques(): List<BloqueHorario> = catalogoDao.listarBloques()

    fun rangosEtarios(): List<Pair<RangoEtario, String>> = catalogoDao.listarRangosEtarios()

    fun companias(): List<Compania> = catalogoDao.listarCompanias()

    fun fechaHoy(): String = catalogoDao.fechaHoy()

    fun fechaDeTrabajo(): String = catalogoDao.fechaDeTrabajo()

    // ---- personas ----

    fun colaboradorPorRut(rut: String): Colaborador? = usuarioDao.buscarColaboradorPorRut(rut)

    fun colaboradorPorId(id: Long): Colaborador? = usuarioDao.buscarColaboradorPorId(id)

    fun cambiarNombre(idUsuario: Long, nombres: String, apellidoPaterno: String): Boolean =
        usuarioDao.cambiarNombre(idUsuario, nombres, apellidoPaterno) > 0

    // ---- sesiones de conteo ----

    fun sesionEnCursoDe(idObservador: Long): SesionResumen? = sesionDao.sesionEnCursoDe(idObservador)

    fun sesionesDeFecha(fecha: String): List<SesionResumen> = sesionDao.sesionesDeFecha(fecha)

    fun sesion(idSesion: Long): SesionResumen? = sesionDao.buscarSesion(idSesion)

    fun detalleDeSesion(idSesion: Long): DetalleSesion? = sesionDao.detalleDeSesion(idSesion)

    fun sesionesPorValidar(): List<SesionResumen> = sesionDao.sesionesPorValidarTodas()

    fun sesionesPorValidar(fecha: String): List<SesionResumen> = sesionDao.sesionesPorValidar(fecha)

    fun sesionesDeLosUltimosDias(desde: String, dias: Int): List<SesionResumen> =
        sesionDao.sesionesDeLosUltimosDias(desde, dias)

    fun resumenDeSesion(idSesion: Long): ResumenSesion = sesionDao.resumenDeSesion(idSesion)

    fun iniciarSesion(idSesion: Long, hora: String): Int = sesionDao.iniciarSesion(idSesion, hora)
    fun observadores(): List<Colaborador> = usuarioDao.listarObservadores()

    fun colaboradores(): List<Colaborador> = usuarioDao.listarColaboradores()

    fun programarSesion(
        idAcceso: Long,
        fecha: String,
        horaInicio: String,
        horaFin: String,
        idObservador: Long,
        idProgramador: Long
    ): String = sesionDao.programarSesion(idAcceso, fecha, horaInicio, horaFin, idObservador, idProgramador)

    fun cerrarSesion(idSesion: Long, hora: String): Int = sesionDao.cerrarSesionReal(idSesion, hora)

    fun revisarSesion(idSesion: Long, estado: EstadoSesion, idRevisor: Long, motivo: String? = null): Int =
        sesionDao.revisarSesion(idSesion, estado, idRevisor, motivo)

    // ---- registros ----

    fun registrar(
        idSesion: Long,
        clasificacion: Clasificacion,
        rangoEtario: RangoEtario,
        compania: Compania,
        comprasOtraTienda: Boolean,
        seDirige: Long? = null
    ): Long = sesionDao.insertarRegistro(idSesion, clasificacion, rangoEtario, compania, comprasOtraTienda, seDirige)

    fun anularRegistro(idRegistro: Long, motivo: String, idColaborador: Long): Int =
        sesionDao.anularRegistro(idRegistro, motivo, idColaborador)

    fun ultimosRegistros(idSesion: Long, limite: Int = 8): List<Registro> =
        sesionDao.ultimosRegistros(idSesion, limite)

    fun indicadoresDeFecha(fecha: String): IndicadoresDia? = indicadorDao.indicadoresDeFecha(fecha)

    fun indicadoresDeTipoDia(tipo: String, desde: String? = null, hasta: String? = null): IndicadoresDia? =
        indicadorDao.indicadoresDeTipoDia(tipo, desde, hasta)

    fun transitoPorAccesoDeTipo(tipo: String, desde: String? = null, hasta: String? = null): List<TransitoPorAcceso> =
        indicadorDao.transitoPorAccesoDeTipo(tipo, desde, hasta)

    fun semanasConDatos(): List<Pair<String, String>> = indicadorDao.semanasConDatos()

    // La medición del observador no depende de la carga de la tienda: se puede mostrar sola.
    fun medicionDeFecha(fecha: String): MedicionPeriodo = indicadorDao.medicionDeFecha(fecha)

    fun medicionDeTipo(tipo: String, desde: String? = null, hasta: String? = null): MedicionPeriodo =
        indicadorDao.medicionDeTipo(tipo, desde, hasta)


    fun transitoPorAcceso(fecha: String): List<TransitoPorAcceso> = indicadorDao.transitoPorAcceso(fecha)

    fun bloquesPendientes(fecha: String): List<String> = indicadorDao.bloquesPendientes(fecha)

    fun reporteJefatura(): ReporteJefatura = indicadorDao.reporteJefatura(fechaHoy())

    // Mientras haya datos de ejemplo, los informes lo dicen y el administrador puede borrarlos.
    // Cola de salida de los registros anotados sin red.
    fun registrosPendientes(limite: Int = 200): List<RegistroPendiente> = colaDao.pendientes(limite)

    fun marcarSesionRemota(idLocal: Long, idRemoto: Long) = sesionDao.marcarRemota(idLocal, idRemoto)

    fun idRemotoDeSesion(idLocal: Long): Long? = sesionDao.idRemotoDe(idLocal)

    fun idDeSesionPorCodigo(codigo: String): Long = sesionDao.idPorCodigo(codigo)

    fun contarRegistrosPendientes(): Int = colaDao.contarPendientes()

    fun marcarRegistroEnviado(idLocal: Long, idRemoto: Long) = colaDao.marcarEnviado(idLocal, idRemoto)

    fun datosDeEjemplo(): Boolean = mantenimientoDao.datosDeEjemplo()

    fun borrarDatosDeEjemplo(): Boolean = mantenimientoDao.borrarDatosDeEjemplo()

    fun cargaDelDia(fecha: String = fechaHoy()): CargaDia = cargaDao.cargaDeFecha(fecha)

    fun guardarCargaDelDia(carga: CargaDia, idUsuario: Long): Boolean =
        cargaDao.guardar(carga, idUsuario, ahora())

    fun guardarMeta(anio: Int, mes: Int, porcentaje: Double, idUsuario: Long): Boolean =
        indicadorDao.guardarMeta(anio, mes, porcentaje, idUsuario)

    // ---- eventos del dia ----

    fun registrarEvento(evento: EventoDia): Long = eventoDao.registrarEvento(evento)

    fun eventosRegistrados(): List<EventoDia> = eventoDao.eventosRegistrados()

    fun anularEvento(idEvento: Long, motivo: String, idColaborador: Long): Int =
        eventoDao.anularEvento(idEvento, motivo, idColaborador)

    fun contarEventos(fecha: String): Int = eventoDao.contarEventos(fecha)

    fun contarTodosLosEventos(): Int = eventoDao.contarTodosLosEventos()

    fun preguntas(): List<PreguntaSeguridad> = usuarioDao.listarPreguntas()

    // El administrador crea la cuenta con un código temporal que vence en una semana.
    fun crearColaborador(datos: NuevaPersona, codigo: String): Long? =
        usuarioDao.crearColaborador(
            datos,
            Clave.hash(Rut.formatear(datos.rut), codigo),
            vencimientoDeCodigo()
        )

    fun actualizarColaborador(
        idUsuario: Long,
        datos: NuevaPersona,
        perfil: PerfilUsuario,
        genero: Genero,
        activo: Boolean
    ): Boolean = usuarioDao.actualizarColaborador(idUsuario, datos, perfil, genero, activo)

    fun regenerarCodigo(idUsuario: Long, rut: String, codigo: String): Boolean =
        usuarioDao.regenerarCodigo(idUsuario, Clave.hash(rut, codigo), vencimientoDeCodigo())

    fun definirClaveDefinitiva(idUsuario: Long, rut: String, clave: String, idPregunta: Long, respuesta: String): Boolean {
        val claveOk = usuarioDao.definirClaveDefinitiva(idUsuario, Clave.hash(rut, clave)) ||
            return false
        usuarioDao.guardarRespuestas(idUsuario, rut, listOf(idPregunta to respuesta))
        return claveOk
    }

    private fun vencimientoDeCodigo(): String =
        java.time.LocalDate.now().plusDays(7).toString()
    fun preguntasDe(idUsuario: Long): List<PreguntaSeguridad> = usuarioDao.listarPreguntasDe(idUsuario)
    fun tienePreguntas(idUsuario: Long): Boolean = usuarioDao.tienePreguntas(idUsuario)
    fun guardarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>) =
        usuarioDao.guardarRespuestas(idUsuario, rut, respuestas)
    fun verificarRespuestas(idUsuario: Long, rut: String, respuestas: List<Pair<Long, String>>): Boolean =
        usuarioDao.verificarRespuestas(idUsuario, rut, respuestas)
    fun cambiarClave(idUsuario: Long, nuevoHash: String): Boolean = usuarioDao.cambiarClave(idUsuario, nuevoHash)

    fun fotoDe(idUsuario: Long): ByteArray? = usuarioDao.fotoDe(idUsuario)

    fun guardarFoto(idUsuario: Long, foto: ByteArray?): Boolean = usuarioDao.guardarFoto(idUsuario, foto)

    fun cerrar() = db.close()
}
