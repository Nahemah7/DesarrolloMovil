package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.BloqueHorario
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.ConversionMes
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.model.HoraPunta
import com.example.rinconsur.model.MetaDelMes
import com.example.rinconsur.model.RangoEtario
import com.example.rinconsur.model.PersonalRendimiento
import com.example.rinconsur.model.ReporteJefatura

fun ReporteRemoto.aReporteJefatura(bloques: List<BloqueHorario>): ReporteJefatura {
    val porId = bloques.associateBy { it.id.toInt() }
    return ReporteJefatura(
        meta = MetaDelMes(
            anio = meta?.anio ?: 0,
            mes = meta?.mes ?: 0,
            objetivo = meta?.objetivo,
            actual = meta?.actual,
            dias = meta?.dias ?: 0
        ),
        deEjemplo = false,
        meses = meses.map { ConversionMes(it.mes, it.ingresos, it.boletas, it.conversion, it.dias) },
        horas = horas.map {
            HoraPunta(
                hora = porId[it.idBloque]?.horaInicioCorta ?: ("bloque " + it.idBloque),
                ingresos = it.ingresos,
                transito = it.transito,
                medidos = it.medidos,
                bloques = it.bloques
            )
        },
        porRango = porRango.map {
            GrupoConteo(RangoEtario.porId(it.etiqueta.toLongOrNull() ?: 0).etiqueta, it.cantidad, it.porcentaje)
        },
        porCompania = porCompania.map {
            GrupoConteo(Compania.porId(it.etiqueta.toLongOrNull() ?: 0).etiqueta, it.cantidad, it.porcentaje)
        },
        compras = compras.map {
            GrupoConteo(
                if (it.etiqueta == "si") "Compró en otra tienda" else "No compró en otra tienda",
                it.cantidad,
                it.porcentaje
            )
        }
    )
}

fun PersonalRemoto.aRendimiento(): PersonalRendimiento = PersonalRendimiento(
    idObservador = idObservador?.toLong() ?: 0L,
    observador = observador.orEmpty(),
    sesiones = (sesiones ?: 0L).toInt(),
    cerradas = (cerradas ?: 0L).toInt(),
    validadas = (validadas ?: 0L).toInt(),
    rechazadas = (rechazadas ?: 0L).toInt(),
    registros = (registros ?: 0L).toInt(),
    transito = (transito ?: 0L).toInt(),
    ultimaFecha = ultimaFecha
)

fun SupervisorRemoto.aSupervisor(): com.example.rinconsur.model.RendimientoSupervisor =
    com.example.rinconsur.model.RendimientoSupervisor(
        idSupervisor = idSupervisor?.toLong() ?: 0L,
        programadas = (programadas ?: 0L).toInt(),
        pendientes = (pendientes ?: 0L).toInt(),
        validadas = (validadas ?: 0L).toInt(),
        rechazadas = (rechazadas ?: 0L).toInt()
    )
