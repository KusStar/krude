package com.kuss.krude.utils

import androidx.compose.ui.Modifier

fun Modifier.applyIf(condition : Boolean, trueModifier : Modifier.() -> Modifier) : Modifier {
    return applyIfElse(condition, trueModifier) { this }
}

fun Modifier.applyIfElse(condition : Boolean, trueModifier : Modifier.() -> Modifier, falseModifier: Modifier.() -> Modifier): Modifier {
    return if (condition) {
        then(trueModifier(Modifier))
    } else {
        then(falseModifier(Modifier))
    }
}