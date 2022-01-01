package com.kuss.krude.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object AsyncHelper {
    @JvmStatic
    fun doAsyncUI(fn: () -> Unit) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())

        executor.execute {
            handler.post {
                fn()
            }
        }
    }

    @JvmStatic
    fun doAsync(fn: () -> Unit) {
        val executor: ExecutorService = Executors.newSingleThreadExecutor()
        executor.execute {
            fn()
        }
    }

}