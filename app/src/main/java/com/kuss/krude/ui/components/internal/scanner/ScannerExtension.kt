package com.kuss.krude.ui.components.internal.scanner

import android.Manifest
import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.kuss.krude.utils.DynamicFeatureUtils

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScannerExtension(focusRequester: FocusRequester) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionStates = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
        )
    )
    var hasPermission by remember {
        mutableStateOf(false)
    }
    val view = LocalView.current
    DisposableEffect(key1 = lifecycleOwner, effect = {
        val window = (view.context as Activity).window
        val originalStatusColor = window.statusBarColor
        val originalNavbarColor = window.navigationBarColor
        window.statusBarColor = Color.Black.toArgb()
        window.navigationBarColor = Color.Black.toArgb()

        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                permissionStates.launchMultiplePermissionRequest()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            window.statusBarColor = originalStatusColor
            window.navigationBarColor = originalNavbarColor

            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    })
    LaunchedEffect(permissionStates) {
        permissionStates.permissions.forEach { it ->
            when (it.permission) {
                Manifest.permission.CAMERA -> {
                    when {
                        it.hasPermission -> {
                            hasPermission = true
                        }
                    }
                }
            }
        }
    }
    Box(
        modifier = Modifier
            .focusRequester(focusRequester)
            .fillMaxSize()
    ) {
        if (hasPermission) {
            CameraView()
        }
    }
}

@Composable
fun CameraView() {
    DynamicFeatureUtils.dfCameraView(PaddingValues())
}
