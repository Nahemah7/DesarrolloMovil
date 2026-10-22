package com.example.rinconsur.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Recurso nativo de vibracion: la segunda API que pide la entrega.
 *
 * Se justifica en el propio caso: el observador registra de pie y mirando a la persona, con una
 * mano. El aviso corto confirma que el registro quedo guardado sin tener que mirar la pantalla,
 * que es justo lo que pide registrar en menos de 3 segundos (RNF-01).
 */
object Vibracion {

    private const val DURACION_MS = 45L

    fun confirmarGuardado(context: Context) {
        val vibrador = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        } ?: return

        if (!vibrador.hasVibrator()) return
        vibrador.vibrate(VibrationEffect.createOneShot(DURACION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
