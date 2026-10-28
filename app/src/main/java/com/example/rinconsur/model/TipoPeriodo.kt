package com.example.rinconsur.model

/**
 * Lo que se pide en la pantalla de indicadores: el dia de hoy, o el consolidado de los dias
 * habiles y de los fines de semana que la tienda ya cargo.
 *
 * El tipo de cada fecha lo decide la vista `v_tipo_dia` (sabado, domingo y feriado son "Fin de
 * semana"), asi que la app no repite esa regla: solo pide el consolidado por tipo.
 */
enum class TipoPeriodo(val etiqueta: String, val tipoDiaBd: String?) {
    HOY("Hoy", null),
    HABILES("Hábiles", "Habil"),
    FIN_DE_SEMANA("Fin de semana", "Fin de semana")
}
