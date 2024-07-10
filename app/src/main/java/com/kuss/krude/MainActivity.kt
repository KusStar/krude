package com.kuss.krude

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kuss.krude.ui.AppEntry
import com.kuss.krude.ui.theme.AppTheme
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.LocaleHelper
import com.kuss.krude.utils.PinyinHelper
import com.kuss.krude.utils.Umami
import timber.log.Timber


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        Timber.plant(object: Timber.DebugTree() {
            override fun createStackElementTag(element: StackTraceElement): String? {
                val className = super.createStackElementTag(element)
                return "KRUDE-$className ${element.methodName}"
            }
        })

        LocaleHelper.init(this)

        PinyinHelper.initDict()

        ActivityHelper.initActivity(this)

        Umami.trackInit()

        setContent {
            Box(Modifier.safeDrawingPadding()) {
                AppTheme {
                    AppEntry()
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ActivityHelper.initActivity(this)
    }

}



