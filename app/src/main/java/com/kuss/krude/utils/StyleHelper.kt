package com.kuss.krude.utils

import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle

fun measureMaxWidthOfTexts(textMeasurer: TextMeasurer, texts: List<String>, style: TextStyle): Int {
    var maxWidth = 0
    texts.forEach {
        val widthInPixels = textMeasurer.measure(
            it,
            style = style
        ).size.width
        if (widthInPixels > maxWidth) {
            maxWidth = widthInPixels
        }
    }
    return maxWidth
}