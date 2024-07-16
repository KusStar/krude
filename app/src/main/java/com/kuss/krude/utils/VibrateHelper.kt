package com.kuss.krude.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator

object VibrateHelper {
    private const val VIBRATE_DURATION = 200L

    fun onScan(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        try {
            vibrator.vibrate(
                VibrationEffect.createOneShot(
                    VIBRATE_DURATION,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}