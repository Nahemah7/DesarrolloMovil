package com.example.rinconsur.data

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

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
