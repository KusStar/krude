package com.kuss.krude.ui.components.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun StarBox(showStar: Boolean, content: @Composable () -> Unit) {
    Box {
        content()
        if (showStar) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(3.5.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Star,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    contentDescription = "Star",
                    modifier = Modifier
                        .size(14.dp)
                        .background(MaterialTheme.colorScheme.onPrimaryContainer, CircleShape)
                        .padding(2.dp)
                )
            }
        }
    }
}