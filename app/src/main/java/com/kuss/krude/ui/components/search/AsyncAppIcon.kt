package com.kuss.krude.ui.components.search

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.kuss.krude.R
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import me.zhanghai.android.appiconloader.AppIconLoader

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
                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.MATCH_ARCHIVED_PACKAGES.toInt()
                } else {
                    0
                }
                val info = packageManager.getApplicationInfo(packageName, flags)

                val iconSize = context.resources.getDimensionPixelSize(R.dimen.app_icon_size)
                val icon = AppIconLoader(iconSize, true, context).loadIcon(info)

                bitmap.value = icon.asImageBitmap()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    bitmap.value?.let { bitmap ->
        Image(
            bitmap = bitmap,
            contentDescription = packageName,
            modifier = modifier
        )
    }
//    Crossfade(targetState = bitmap.value != null, label = "appicon") {
//        if (it) {
//            Image(
//                bitmap = bitmap.value!!,
//                contentDescription = null,
//                modifier = modifier
//            )
//        } else {
//            Box(
//                modifier = modifier
//            )
//        }
//
//    }
}