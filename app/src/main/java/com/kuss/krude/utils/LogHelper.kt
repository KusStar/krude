package com.kuss.krude.utils

import android.util.Log
import com.kuss.krude.BuildConfig

object LogHelper {
    inline fun d(lazyMessage: () -> Any?) {
        if (BuildConfig.DEBUG) {
            Log.d("KRUDE_DEBUG", lazyMessage().toString())
        }
    }
}