package com.kuss.krude

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.kuss.krude.ui.AppList
import com.kuss.krude.ui.theme.AppTheme
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.LocaleHelper
import com.kuss.krude.utils.PinyinHelper


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PinyinHelper.initDict()


        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent),
                ) {
                    AppList()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (!ActivityHelper.isDefaultLauncher(this)) {
            super.onBackPressed()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.setLocale(base, myLang))
    }

    companion object {
        var myLang: String = "zh"
    }
}



