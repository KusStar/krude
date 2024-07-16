package com.kuss.krude.ui.components.internal.scanner

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import timber.log.Timber
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private suspend fun Context.getCameraProvider(): ProcessCameraProvider =
    suspendCoroutine { continuation ->
        ProcessCameraProvider.getInstance(this).also { cameraProvider ->
            cameraProvider.addListener({
                continuation.resume(cameraProvider.get())
            }, ContextCompat.getMainExecutor(this))
        }
    }

fun animAlpha(animatedValue: Float): Float {
    return if (animatedValue < 0.2f) {
        val a = 0.0f
        val b = 0.2f
        val c = 0.0f
        val d = 1.0f
        c + (d - c) * (animatedValue - a) / (b - a)
    } else if (animatedValue > 0.8f) {
        val a = 0.8f
        val b = 1.0f
        val c = 1.0f
        val d = 0.0f
        c + (d - c) * (animatedValue - a) / (b - a)
    } else {
        1.0f
    }
}

@Composable
fun ScanLine(
    color: Color = Color.Cyan,
    tweenDuration: Int = 2000,
    scanLineWidth: Int = 1,
    gradientHeight: Float = 48f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "scanline")
    val animatedValue by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f, animationSpec = infiniteRepeatable(
            animation = tween(tweenDuration, easing = LinearEasing), repeatMode = RepeatMode.Restart
        ), label = "scanline"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height

        val linePosition = canvasHeight * animatedValue

        val alpha = animAlpha(animatedValue)

        drawLine(
            color = color.copy(alpha = alpha / 2),
            start = Offset(x = 0f, y = linePosition),
            end = Offset(x = canvasWidth, y = linePosition),
            strokeWidth = scanLineWidth.dp.toPx()
        )

        val brush = Brush.verticalGradient(
            0f to Color.Transparent,
            1f to color.copy(alpha = alpha * 0.3f),
            startY = linePosition - gradientHeight,
            endY = linePosition
        )

        drawRect(
            brush,
            size = Size(width = canvasWidth, height = gradientHeight),
            topLeft = Offset(x = 0f, y = linePosition - gradientHeight),
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun ScanLinePreview() {
    Box(modifier = Modifier.size(256.dp)) {
        ScanLine()
    }
}

@Composable
fun CameraPreview() {
    val lensFacing = CameraSelector.LENS_FACING_BACK
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val preview = remember {
        Preview.Builder().build()
    }
    val previewView = remember {
        PreviewView(context)
    }
    val cameraxSelector = remember {
        CameraSelector.Builder().requireLensFacing(lensFacing).build()
    }
    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val observer = object : DefaultLifecycleObserver {
            var cameraProvider: ProcessCameraProvider? = null

            override fun onCreate(owner: LifecycleOwner) {
                cameraProviderFuture.addListener({
                    cameraProvider = cameraProviderFuture.get()
                    cameraProvider?.unbindAll()
                    try {
                        cameraProvider?.bindToLifecycle(
                            owner, cameraxSelector, preview
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    preview.setSurfaceProvider(previewView.surfaceProvider)
                }, ContextCompat.getMainExecutor(context))
            }

            override fun onDestroy(owner: LifecycleOwner) {
                Timber.d("CameraPreview onDestroy")
                cameraProvider?.unbindAll()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)


        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            observer.cameraProvider?.unbindAll()
            Timber.d("CameraPreview onDispose")
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            { previewView }, modifier = Modifier.fillMaxSize()
        )
        ScanLine()
    }
}

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
            CameraPreview()
        }
    }
}