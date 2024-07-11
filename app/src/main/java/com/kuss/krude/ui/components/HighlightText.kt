package com.kuss.krude.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.withStyle
import com.kuss.krude.ui.theme.warningText
import com.kuss.krude.utils.FilterHelper

fun getHighlightAnnotatedString(
    text: String,
    highlightText: String,
    highlightTextColor: Color
): AnnotatedString {
    val builder = AnnotatedString.Builder()

    val startIndex = FilterHelper.getAbbrForChinese(text).indexOf(highlightText, ignoreCase = true)
    if (startIndex != -1) {
        val endIndex = startIndex + highlightText.length

        builder.append(text.subSequence(0, startIndex))
        builder.withStyle(style = SpanStyle(color = highlightTextColor)) {
            builder.append(text.subSequence(startIndex, endIndex))
        }
        builder.append(text.subSequence(endIndex, text.length))
    } else {
        builder.append(text)
    }
    return builder.toAnnotatedString()
}

@Composable
fun HighlightedText(
    modifier: Modifier = Modifier,
    text: String,
    highlightText: String,
    highlightTextColor: Color = MaterialTheme.colorScheme.warningText,
    textColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle
) {
    val annotatedString = remember(text, highlightText) {
        getHighlightAnnotatedString(text, highlightText, highlightTextColor)
    }

    Text(modifier = modifier, text = annotatedString, style = style, color = textColor)
}