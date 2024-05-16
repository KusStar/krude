package com.kuss.krude.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ToastUtils {
    private val handler = Handler(Looper.getMainLooper())
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            Toast.makeText(context, message, duration).show()
            return
        }
        handler.post {
            Toast.makeText(context, message, duration).show()
        }
    }
}