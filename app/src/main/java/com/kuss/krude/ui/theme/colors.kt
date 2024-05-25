package com.kuss.krude.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext


val defaultColors = lightColorScheme()

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val colors = if (dynamicColor) {
        if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        defaultColors
    }

//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colors.primary.toArgb()
//            window.navigationBarColor = colors.primary.toArgb()
//            WindowCompat
//                .getInsetsController(window, view)
//                .isAppearanceLightStatusBars = useDarkTheme
//            WindowCompat
//                .getInsetsController(window, view)
//                .isAppearanceLightNavigationBars = useDarkTheme
//        }
//    }

    MaterialTheme(
        colorScheme = colors,
        content = content,
    )
}

