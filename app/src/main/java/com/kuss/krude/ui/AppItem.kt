package com.kuss.krude.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.data.AppInfoWithIcon
import com.valentinilk.shimmer.shimmer

@Composable
fun AppItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    item: AppInfoWithIcon,
    showSubtitle: Boolean = true,
    titleSingleLine: Boolean = false,
    enabled: Boolean = true,
    iconSize: Dp = 48.dp,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
) {
    CustomButton(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Log.d("AppItem", "onLongPress")
                    }
                )
            },
        shape = RoundedCornerShape(8.dp),
        enabled = enabled
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                bitmap = item.icon.asImageBitmap(),
                contentDescription = "icon",
                modifier = Modifier
                    .size(iconSize)
            )
            Spacing(1)
            Text(
                text = item.label,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = titleFontSize,
                maxLines = if (titleSingleLine) 1 else Int.MAX_VALUE,
                overflow = TextOverflow.Ellipsis,
            )
            if (showSubtitle) {
                Spacing(1, 4)
                Text(
                    text = item.packageName,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = subtitleFontSize,
                )
            }
        }
    }
}

@Composable
fun AppItemShimmer(
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            // This example uses a custom modifier defined in CustomModifierSample.kt
            modifier = Modifier.shimmer(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val titleDp = with(LocalDensity.current) {
                titleFontSize.toDp()
            }
            val subtitleDp = with(LocalDensity.current) {
                subtitleFontSize.toDp()
            }
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp))
            )
            Spacing(1)
            Box(
                modifier = Modifier
                    .height(titleDp)
                    .width(48.dp)
                    .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(16.dp))
            )
            Spacing(1, 4)
            Box(
                modifier = Modifier
                    .height(subtitleDp)
                    .width(32.dp)
                    .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(16.dp))
            )
        }
    }
}