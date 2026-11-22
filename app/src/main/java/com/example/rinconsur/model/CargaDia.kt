package com.example.rinconsur.model

data class BloqueCarga(
    val idBloque: Long,
    val hora: String,
    val ingresos: String = "",
    val boletas: String = "",
    val detalle: Map<Long, String> = emptyMap(),
    val detallado: Boolean = false
) {
    val ingresosNumero: Int? get() = ingresos.trim().toIntOrNull()
    val boletasNumero: Int? get() = boletas.trim().toIntOrNull()

    val sumaDetalle: Int
        get() = detalle.values.sumOf { it.trim().toIntOrNull() ?: 0 }

    val detalleCuadra: Boolean
        get() = !detallado || sumaDetalle == (ingresosNumero ?: 0)

    val conIngresos: Boolean get() = (ingresosNumero ?: 0) > 0 || ingresos.trim() == "0"
    val conBoletas: Boolean get() = boletasNumero != null
    val conDatos: Boolean get() = conIngresos || conBoletas

    fun conDetalle(acceso: Long, valor: String): BloqueCarga =
        copy(detalle = detalle + (acceso to valor), detallado = true)

    fun sinDetalle(): BloqueCarga = copy(detalle = emptyMap(), detallado = false)
}

data class CargaDia(
    val fecha: String,
    val accesos: List<Acceso> = emptyList(),
    val bloques: List<BloqueCarga> = emptyList()
) {
    val totalIngresos: Int get() = bloques.sumOf { it.ingresosNumero ?: 0 }
    val totalBoletas: Int get() = bloques.sumOf { it.boletasNumero ?: 0 }

    val conversion: Double?
        get() = if (totalIngresos <= 0) null
        else Math.round(1000.0 * totalBoletas / totalIngresos) / 10.0

    val conversionTexto: String
        get() = conversion?.let { formatoPorcentaje(it) } ?: "sin dato"

    val bloquesConIngresos: Int get() = bloques.count { it.conIngresos }
    val bloquesConBoletas: Int get() = bloques.count { it.conBoletas }
    val bloquesConDetalle: Int get() = bloques.count { it.detallado }
    val bloquesConDatos: Int get() = bloques.count { it.conDatos }

    val bloquesAPartar: Int get() = bloques.count { it.conIngresos != it.conBoletas }

    val errores: List<String>
        get() = buildList {
            bloques.forEach { bloque ->
                if (bloque.ingresos.isNotBlank() && bloque.ingresosNumero == null) {
                    add(bloque.hora + ": los ingresos tienen que ser un número.")
                }
                if (bloque.boletas.isNotBlank() && bloque.boletasNumero == null) {
                    add(bloque.hora + ": las boletas tienen que ser un número.")
                }
                if (bloque.detallado && !bloque.detalleCuadra) {
                    add(bloque.hora + ": el reparto por acceso suma " + bloque.sumaDetalle +
                        " y el total del bloque es " + (bloque.ingresosNumero ?: 0) + ".")
                }
            }
        }

    val puedeGuardar: Boolean get() = errores.isEmpty() && bloques.any { it.conDatos }

    fun bloque(idBloque: Long): BloqueCarga? = bloques.firstOrNull { it.idBloque == idBloque }

    fun conIngresos(idBloque: Long, valor: String): CargaDia =
        cambiar(idBloque) { it.copy(ingresos = valor) }

    fun conBoletas(idBloque: Long, valor: String): CargaDia =
        cambiar(idBloque) { it.copy(boletas = valor) }

    fun conDetalle(idBloque: Long, idAcceso: Long, valor: String): CargaDia =
        cambiar(idBloque) { it.conDetalle(idAcceso, valor) }

    fun alternarDetalle(idBloque: Long): CargaDia =
        cambiar(idBloque) { if (it.detallado) it.sinDetalle() else it.copy(detallado = true) }

    private fun cambiar(idBloque: Long, cambio: (BloqueCarga) -> BloqueCarga): CargaDia =
        copy(bloques = bloques.map { if (it.idBloque == idBloque) cambio(it) else it })
}
