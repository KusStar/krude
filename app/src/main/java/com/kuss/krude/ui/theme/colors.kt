package com.kuss.krude.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.kuss.krude.utils.ActivityHelper


val defaultColors = lightColorScheme(
    primary = Color.White,
    secondary = Color.Gray,
    surface = Color.Transparent
)

@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val dynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val context = LocalContext.current
    val activity = LocalContext.current as Activity
    val colors = if (dynamicColor) {
        val final =
            if (useDarkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        if (ActivityHelper.isDefaultLauncher(activity)) {
            final.copy(
                surface = Color.Transparent,
                primary = final.primaryContainer,
                secondary = final.secondaryContainer,
                tertiary = final.tertiaryContainer
            )
        } else {


            final
        }
    } else {
        defaultColors
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

