package com.kuss.krude.ui.components.internal.scanner

import android.Manifest
import android.app.Activity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.kuss.krude.utils.VibrateHelper
import java.util.concurrent.Executors

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
fun CameraPreview(modifier: Modifier = Modifier) {
    val localContext = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val localDensity = LocalDensity.current

    val lifecycleCameraController = remember {
        LifecycleCameraController(localContext).apply {
            bindToLifecycle(lifecycleOwner)
            cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        }
    }

    val previewView = remember {
        PreviewView(localContext).apply {
            controller = lifecycleCameraController
        }
    }

    var barcodes by remember {
        mutableStateOf<List<Barcode>>(listOf())
    }

    LaunchedEffect(previewView) {
        val barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build()
        )

        // thanks: https://stackoverflow.com/a/77698616
        val mlKitAnalyzer = MlKitAnalyzer(
            listOf(barcodeScanner),
            ImageAnalysis.COORDINATE_SYSTEM_VIEW_REFERENCED,
            ContextCompat.getMainExecutor(localContext),
        ) { result ->
            if (barcodes.isNotEmpty()) {
                return@MlKitAnalyzer
            }
            result.getValue(barcodeScanner)?.filter { !it.rawValue.isNullOrEmpty() }?.let {
                if (it.isNotEmpty()) {
                    barcodes = it
                    VibrateHelper.onScan(localContext)
                }
            }
        }

        lifecycleCameraController.apply {
            setImageAnalysisAnalyzer(
                Executors.newSingleThreadExecutor(),
                mlKitAnalyzer,
            )
            setEnabledUseCases(CameraController.IMAGE_ANALYSIS)
        }
    }

    Box {
        AndroidView(
            modifier = modifier.fillMaxSize(),
            factory = { previewView },
        )
        ScanLine()
        AnimatedVisibility(barcodes.isNotEmpty()) {
            barcodes.forEach { barcode ->
                val boundingBox = barcode.boundingBox
                if (boundingBox != null) {
                    val x = with(localDensity) { boundingBox.left.toDp() }
                    val y = with(localDensity) { boundingBox.top.toDp() }
                    val width = with(localDensity) { boundingBox.width().toDp() }
                    val height = with(localDensity) { boundingBox.height().toDp() }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .offset(x = x, y = y)
                            .size(width = width, height = height)
                    ) {
                        Box(
                            Modifier
                                .size(24.dp)
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                                .border(2.dp, Color.White, CircleShape)
                        )
//                        Text(
//                            text = barcode.rawValue.orEmpty(),
//                            color = MaterialTheme.colorScheme.successText
//                        )
                    }
                }
            }

            Box(
                contentAlignment = Alignment.BottomCenter,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                Button(
                    onClick = {
                        barcodes = listOf()
                    }) {
                    Text(text = "Rescan")
                }
            }
        }
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