package com.example.rinconsur.model

// Una porción del donut: cuánto ocupa y dónde empieza, para poder dibujarla sin cuentas en la pantalla.
data class Porcion(
    val etiqueta: String,
    val valor: Int,
    val porcentaje: Double,
    val inicioGrados: Float,
    val barridoGrados: Float
)

// Reparte un círculo completo entre los valores. Si no hay datos, el círculo queda para el vacío.
fun porciones(datos: List<Pair<String, Int>>): List<Porcion> {
    val total = datos.sumOf { it.second }
    if (total <= 0) return emptyList()

    var acumulado = 0
    var inicio = -90f   // empieza arriba
    return datos.map { (etiqueta, valor) ->
        val barrido = if (valor <= 0) 0f else (valor.toFloat() / total) * 360f
        val porcion = Porcion(
            etiqueta = etiqueta,
            valor = valor,
            porcentaje = Math.round(1000.0 * valor / total) / 10.0,
            inicioGrados = inicio,
            barridoGrados = barrido
        )
        acumulado += valor
        inicio += barrido
        porcion
    }
}

// Altura relativa de una barra frente al máximo de la serie (0 a 1).
fun alturaRelativa(valor: Int, maximo: Int): Float {
    if (maximo <= 0 || valor <= 0) return 0f
    return (valor.toFloat() / maximo).coerceIn(0f, 1f)
}
