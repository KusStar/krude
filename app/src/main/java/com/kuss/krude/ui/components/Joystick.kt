package com.kuss.krude.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class JoystickDirection {
    LEFT, RIGHT, UP, DOWN, CENTER
}

fun getDirection(offset: Offset): JoystickDirection {
    if (abs(offset.x) < 0.3 && abs(offset.y) < 0.3) {
        return JoystickDirection.CENTER
    }
    return if (abs(offset.x) > abs(offset.y)) {
        if (offset.x < 0) {
            JoystickDirection.LEFT
        } else {
            JoystickDirection.RIGHT
        }
    } else {
        if (offset.y < 0) {
            JoystickDirection.UP
        } else {
            JoystickDirection.DOWN
        }
    }
}

class JoystickOffsetState {
    var offset by mutableStateOf(IntOffset.Zero)
    var direction by mutableStateOf(JoystickDirection.CENTER)

    fun changeOffset(offset: IntOffset) {
        this.offset = offset
    }

    fun changeDirection(direction: JoystickDirection) {
        this.direction = direction
    }
}

@Composable
fun rememberJoystickOffsetState(): JoystickOffsetState {
    val state = remember {
        JoystickOffsetState()
    }
    return state
}

// thanks: https://github.com/manalkaff/JetStick/tree/552668e3e0b86c2ea3a9aec86a532cac751922fb
// modified from: https://github.com/manalkaff/JetStick/blob/552668e3e0b86c2ea3a9aec86a532cac751922fb/JetStick/src/main/java/com/manalkaff/jetstick/JoyStick.kt#L30
@Composable
fun JoyStick(
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
    dotSize: Dp = 18.dp,
    touchSize: Dp = 96.dp,
    interval: Long = 200,
    onTap: () -> Unit = {},
    moved: (x: Float, y: Float) -> Unit = { _, _ -> },
    onPositionChange: (JoystickDirection) -> Unit = { _ -> },
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    knobColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val scope = rememberCoroutineScope()
    val maxRadius = with(LocalDensity.current) { (size / 2).toPx() }
    val centerX = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
    val centerY = with(LocalDensity.current) { ((size - dotSize) / 2).toPx() }
    var prevOffset by remember { mutableStateOf(Offset(centerY, centerY)) }
    var radius by remember { mutableFloatStateOf(0f) }
    var theta by remember { mutableFloatStateOf(0f) }
    val animPos = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    var isTapped by remember { mutableStateOf(false) }
    val knobScale by animateFloatAsState(if (isTapped) 1.3f else 1.0f, label = "knotScale")
    var dragging by remember { mutableStateOf(false) }
    var draggingJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(dragging) {
        if (dragging) {
            draggingJob = scope.launch {
                delay(66)
                onPositionChange(
                    getDirection(animPos.value)
                )
                delay(interval)
                while (true) {
                    onPositionChange(
                        getDirection(animPos.value)
                    )
                    delay(interval)
                }
            }
        } else {
            draggingJob?.cancel()
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .background(background, CircleShape)
    ) {
        val offsetX by remember { derivedStateOf { (animPos.value.x + centerX) } }
        val offsetY by remember { derivedStateOf { (animPos.value.y + centerY) } }
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                }
                .size(dotSize)
                .scale(knobScale)
        ) {
            Box(
                modifier = Modifier
                    .size(touchSize)
                    .align(Alignment.Center)
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                dragging = false
                                draggingJob?.cancel()
                                prevOffset = Offset(centerX, centerY)
                                radius = 0f
                                theta = 0f
                                scope.launch {
                                    animPos.animateTo(
                                        Offset.Zero, animationSpec = tween(durationMillis = 300)
                                    )
                                }
                            }
                        ) { pointerInputChange, offset ->
                            dragging = true
                            val x = prevOffset.x + offset.x - centerX
                            val y = prevOffset.y + offset.y - centerY
                            pointerInputChange.consume()
                            theta = if (x >= 0 && y >= 0) {
                                atan(y / x)
                            } else if (x < 0 && y >= 0) {
                                (Math.PI).toFloat() + atan(y / x)
                            } else if (x < 0 && y < 0) {
                                -(Math.PI).toFloat() + atan(y / x)
                            } else {
                                atan(y / x)
                            }
                            radius = sqrt((x.pow(2)) + (y.pow(2)))
                            prevOffset = prevOffset.plus(offset)
                            if (radius > maxRadius) {
                                polarToCartesian(maxRadius, theta)
                            } else {
                                polarToCartesian(radius, theta)
                            }.apply {
                                scope.launch {
                                    animPos.snapTo(Offset(first, second))
                                }
                            }
                        }
                    }
                    .onGloballyPositioned { coordinates ->
                        moved(
                            (coordinates.positionInParent().x - centerX) / maxRadius,
                            -(coordinates.positionInParent().y - centerY) / maxRadius
                        )
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .align(Alignment.Center)
                        .shadow(elevation = 1.dp, shape = CircleShape)
                        .background(knobColor, CircleShape)
                )
            }
        }
    }
}


private fun polarToCartesian(radius: Float, theta: Float): Pair<Float, Float> =
    Pair(radius * cos(theta), radius * sin(theta))

@Preview
@Composable
fun PreviewJoystick() {
    JoyStick()
}