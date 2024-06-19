package com.kuss.krude.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat


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

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.primary.toArgb()
            WindowCompat
                .getInsetsController(window, view)
                .isAppearanceLightStatusBars = useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

// from: https://github.com/emilkowalski/sonner/blob/a2bbec05521efddbe42794a3870b18196255a2c4/src/styles.css#L385

val ColorScheme.successBackground: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.hsl(150f, 1f, 0.06f)
    else Color.hsl(
        143f, 0.85f, 0.96f
    )

val ColorScheme.successText: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.hsl(
        150f, 0.86f, 0.65f
    ) else Color.hsl(140f, 1f, 0.27f)

val ColorScheme.warningBackground: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.hsl(64f, 1f, 0.06f)
    else Color.hsl(
        49f, 1f, 0.97f
    )

val ColorScheme.warningText: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.hsl(46f, 0.87f, 0.65f)
    else Color.hsl(
        31f, 0.92f, 0.45f
    )

val ColorScheme.errorBackground: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.hsl(358f, 0.75f, 0.1f)
    else Color.hsl(
        359f, 1f, 0.97f
    )

val ColorScheme.errorText: Color
    @Composable get() = if (isSystemInDarkTheme()) Color.hsl(358f, 1f, 0.81f)
    else Color.hsl(
        360f, 1f, 0.45f
    )

