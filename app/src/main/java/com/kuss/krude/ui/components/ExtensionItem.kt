package com.kuss.krude.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.interfaces.Extension
import timber.log.Timber

@Composable
fun ExtensionIcon(iconSize: Dp) {
    Icon(
        Icons.Filled.Extension,
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Star",
        modifier = Modifier.size(iconSize)
    )
}

@Composable
fun ExtensionContent(
    item: Extension,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    showTimes: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showStar) {
                Icon(
                    Icons.Filled.Star,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Star",
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = item.name,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = titleFontSize,
            )
        }

        AnimatedVisibility(visible = showTimes) {
            Spacing(1, 4)
            Text(
                text = "${item.priority}${stringResource(id = R.string.open_times)}",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = subtitleFontSize,
            )
        }

        AnimatedVisibility(visible = showSubtitle) {
            Spacing(1, 4)
            Text(
                text = item.description,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = subtitleFontSize,
            )
        }
    }
}

@Composable
fun ExtensionItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    item: Extension,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    enabled: Boolean = true,
    iconSize: Dp = 32.dp,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    showTimes: Boolean = false,
    horizontal: Boolean = false
) {
    CustomButton(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Timber.d("AppItem", "onLongPress")
                    }
                )
            },
        shape = RoundedCornerShape(8.dp),
        enabled = enabled
    ) {
        if (horizontal) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(4.dp)
            ) {
                ExtensionContent(
                    item = item,
                    showStar = showStar,
                    showTimes = showTimes,
                    showSubtitle = showSubtitle,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ExtensionIcon(iconSize)
                Spacing(x = 1)
                ExtensionContent(
                    item = item,
                    showStar = showStar,
                    showTimes = showTimes,
                    showSubtitle = showSubtitle,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize
                )
            }
        }
    }
}
