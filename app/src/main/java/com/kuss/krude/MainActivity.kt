package com.kuss.krude

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.didi.hummer.core.engine.JSValue
import com.didi.hummer.core.engine.napi.NAPIContext
import com.didi.hummer.core.engine.napi.jni.JSException
import com.kuss.krude.ui.AppEntry
import com.kuss.krude.ui.theme.AppTheme
import com.kuss.krude.utils.PinyinHelper
import com.kuss.krude.utils.ToastUtils
import com.kuss.krude.utils.Umami
import timber.log.Timber


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        PinyinHelper.initDict()

        Umami.trackInit()

        setContent {
            AppTheme {
                AppEntry()
            }
        }

        try {
            System.loadLibrary("hummer-napi")
            val jsContext = NAPIContext.create()
            Timber.d("jsContext loaded")

            JSException.addJSContextExceptionCallback(jsContext) { e ->
                Timber.e("JS ERROR")
                Timber.e(e)
            }

            val script = "var v = {" +
                    "   a: 11," +
                    "   b: 111111111111111," +
                    "   c: 12.34," +
                    "   d: true," +
                    "   e: 'Hello Hummer'," +
                    "   f: { aa: 11}," +
                    "   g: () => {},;" +
                    "}; v;"
            jsContext.evaluateJavaScript(script)
            val v: JSValue = jsContext.getJSValue("v")
            Timber.d("jsContext evaluated, v.a = ${v.getInt("a")}")
            ToastUtils.show(this, v.getInt("a").toString())
        } catch (e: Exception) {
            Timber.e("jsContext failed")
            e.printStackTrace()
        }

    }

    companion object {
        var myLang: String = "zh"
    }
}



