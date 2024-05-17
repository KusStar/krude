package com.kuss.krude.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Spacing(x: Int, factor: Int = 8) {
    Spacer(modifier = Modifier.size((x * factor).dp))
}

@Composable
fun Spacing(x: Float, factor: Int = 8) {
    Spacer(modifier = Modifier.size((x * factor).dp))
}