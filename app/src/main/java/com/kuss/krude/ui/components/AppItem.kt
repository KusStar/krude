package com.kuss.krude.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.db.AppInfo
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import me.zhanghai.android.appiconloader.AppIconLoader
import timber.log.Timber

@Composable
fun AsyncAppIcon(packageName: String, modifier: Modifier) {
    val context = LocalContext.current

    val bitmap = remember {
        mutableStateOf<ImageBitmap?>(null)
    }

    LaunchedEffect(packageName) {
        withContext(IO) {
            val packageManager = context.packageManager

            try {
                val info = packageManager.getApplicationInfo(packageName, 0)

                val iconSize = context.resources.getDimensionPixelSize(R.dimen.app_icon_size)
                val icon = AppIconLoader(iconSize, true, context).loadIcon(info)

                bitmap.value = icon.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Crossfade(targetState = bitmap.value != null, label = "appicon") {
        if (it) {
            Image(
                bitmap = bitmap.value!!,
                contentDescription = null,
                modifier = modifier
            )
        } else {
            Box(
                modifier = modifier
            )
        }

    }

}

@Composable
fun AppItemContent(
    item: AppInfo,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    titleSingleLine: Boolean = false,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    showTimes: Boolean = false
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
            text = item.label,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontSize = titleFontSize,
            maxLines = if (titleSingleLine) 1 else Int.MAX_VALUE,
            overflow = TextOverflow.Ellipsis,
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
            text = item.packageName,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary,
            fontSize = subtitleFontSize,
        )
    }
}


@Composable
fun AppItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    item: AppInfo,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    titleSingleLine: Boolean = false,
    enabled: Boolean = true,
    iconSize: Dp = 48.dp,
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
                horizontalArrangement = Arrangement.Center
            ) {
                AsyncAppIcon(
                    packageName = item.packageName, modifier = Modifier
                        .size(iconSize)
                )
                Spacing(1)
                AppItemContent(
                    item = item,
                    showStar = showStar,
                    showSubtitle = showSubtitle,
                    titleSingleLine = true,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize,
                    showTimes = showTimes
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                AsyncAppIcon(
                    packageName = item.packageName, modifier = Modifier
                        .size(iconSize)
                )
                Spacing(1)
                AppItemContent(
                    item = item,
                    showStar = showStar,
                    showSubtitle = showSubtitle,
                    titleSingleLine = titleSingleLine,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize,
                    showTimes = showTimes
                )
            }
        }
    }
}

@Composable
fun AppItemShimmer(
    modifier: Modifier = Modifier,
    iconSize: Dp = 56.dp,
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