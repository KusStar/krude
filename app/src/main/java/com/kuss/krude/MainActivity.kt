package com.kuss.krude

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.kuss.krude.ui.AppEntry
import com.kuss.krude.ui.theme.AppTheme
import com.kuss.krude.utils.LocaleHelper
import com.kuss.krude.utils.PinyinHelper
import com.kuss.krude.utils.Umami
import timber.log.Timber


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
//        installSplashScreen()

        enableEdgeToEdge()

        super.onCreate(savedInstanceState)

        Timber.plant(Timber.DebugTree())

        LocaleHelper.init(this)

        PinyinHelper.initDict()

        Umami.trackInit()

        val attributes = window.attributes

        attributes.gravity = Gravity.BOTTOM

        attributes.width = ViewGroup.LayoutParams.MATCH_PARENT

        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        window.attributes = attributes

        setContent {
            AppTheme {
                AppEntry()
            }
        }
    }

}



