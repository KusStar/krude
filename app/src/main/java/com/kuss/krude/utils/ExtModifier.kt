package com.kuss.krude.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Modifier.applyIf(condition : Boolean, trueModifier : @Composable Modifier.() -> Modifier) : Modifier {
    return applyIfElse(condition, trueModifier) { this }
}

@Composable
fun Modifier.applyIfElse(condition : Boolean, trueModifier : @Composable Modifier.() -> Modifier, falseModifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(trueModifier(Modifier))
    } else {
        then(falseModifier(Modifier))
    }
}

fun Modifier.fadingEdge(brush: Brush) = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawWithContent {
        drawContent()
        drawRect(brush = brush, blendMode = BlendMode.DstIn)
    }