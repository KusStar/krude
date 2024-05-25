package com.kuss.krude.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

object ToastUtils {
    private val handler = Handler(Looper.getMainLooper())
    private var toaster: Toast? = null
    fun show(context: Context, message: String, duration: Int = Toast.LENGTH_SHORT) {
        if (toaster != null) {
            toaster?.cancel()
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            toaster = Toast.makeText(context, message, duration)
            toaster?.show()
            return
        }
        handler.post {
            toaster = Toast.makeText(context, message, duration)
            toaster?.show()
        }
    }
}