package com.kuss.krude

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.kuss.krude.ui.AppEntry
import com.kuss.krude.ui.theme.AppTheme
import com.kuss.krude.utils.LocaleHelper
import com.kuss.krude.utils.PinyinHelper
import com.kuss.krude.utils.Umami
import timber.log.Timber


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        LocaleHelper.init(this)

        PinyinHelper.initDict()

        Umami.trackInit()

        setContent {
            AppTheme {
                AppEntry()
            }
        }
    }

}



