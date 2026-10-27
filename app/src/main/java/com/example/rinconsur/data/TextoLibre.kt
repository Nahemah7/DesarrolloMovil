package com.example.rinconsur.data

/**
 * Regla del texto libre que se escribe en los formularios: descripcion de un evento, "otro motivo"
 * de un rechazo y motivo de una anulacion. Los cuatro campos usan la misma, asi no pasa que uno
 * pida 10 caracteres, otro 5 y otro no ponga limite.
 *
 * El maximo es el largo de las columnas de texto del modelo (VARCHAR(200)).
 */
const val TEXTO_MINIMO = 10
const val TEXTO_MAXIMO = 200

/**
 * Mensaje para mostrar bajo el campo, o null si el texto cumple. Se mide el texto ya sin espacios
 * a los lados, que es el que se guarda. [vacio] es lo que se dice cuando no escribio nada.
 */
fun mensajeDeTextoLibre(texto: String, vacio: String): String? {
    val largo = texto.trim().length
    return when {
        largo == 0 -> vacio
        largo < TEXTO_MINIMO -> "Un poco más de detalle (mínimo " + TEXTO_MINIMO + " caracteres)"
        largo > TEXTO_MAXIMO -> "Máximo " + TEXTO_MAXIMO + " caracteres"
        else -> null
    }
}