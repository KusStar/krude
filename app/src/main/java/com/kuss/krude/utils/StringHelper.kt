package com.kuss.krude.utils

object StringHelper {
    fun middleEllipsis(text: String, maxLength: Int = 24): String {
        if (text.length <= maxLength) return text
        val half = maxLength / 2 - 2
        return text.take(half) + "..." + text.takeLast(half)
    }
}