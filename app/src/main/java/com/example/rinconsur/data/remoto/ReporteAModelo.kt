package com.example.rinconsur.data.remoto

import com.example.rinconsur.model.BloqueHorario
import com.example.rinconsur.model.Compania
import com.example.rinconsur.model.ConversionMes
import com.example.rinconsur.model.GrupoConteo
import com.example.rinconsur.model.HoraPunta
import com.example.rinconsur.model.MetaDelMes
import com.example.rinconsur.model.RangoEtario
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
