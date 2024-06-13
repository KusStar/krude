package com.kuss.krude.ui.components.search

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun getKeymaps(showLeftSideBackspace: Boolean): List<String> {
    val bottomKeys = if (showLeftSideBackspace) "~zxcvbnm-" else "zxcvbnm-"
    return listOf("qwertyuiop", "asdfghjkl", bottomKeys)
}

fun getKeyboardWidth(keyNum: Int, screenWidth: Dp, padding: Dp): Dp {
    val keyWidth = (screenWidth - padding * (keyNum - 2)) / keyNum
    return min(keyWidth, 56.dp)
}

val KEY_PADDING = 3.dp

@Composable
fun SoftKeyboardView(
    showLeftSideBackspace: Boolean,
    onBack: () -> Unit,
    onClick: (key: Char, isDeleting: Boolean) -> Unit,
    offset: Int = 0,
    scale: Float = 1f,
    bottomContent: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val keymaps = remember(showLeftSideBackspace) {
        getKeymaps(showLeftSideBackspace)
    }

    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp

    val keyWidth = remember(screenWidth, keymaps) {
        getKeyboardWidth(keymaps[0].length, screenWidth, KEY_PADDING)
    }

    BackHandler(enabled = true, onBack = onBack)

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
            .scale(scale)
            .absoluteOffset(x = offset.dp)
    ) {
        items(keymaps, key = { it }) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                items(it.toList(), key = { it }) {
                    val interactionSource = remember { MutableInteractionSource() }

                    val isDeleting = it == '-' || it == '~'
                    fun send() {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick(it, isDeleting)
                    }

                    Column(
                        modifier = Modifier
                            .height(keyWidth.times(1.33f))
                            .width(keyWidth)
                            .padding(
                                horizontal = if (isDeleting && showLeftSideBackspace) KEY_PADDING * 2 else KEY_PADDING,
                                vertical = KEY_PADDING * 2
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isDeleting)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.secondary
                            )
                            .indication(
                                interactionSource = interactionSource,
                                LocalIndication.current
                            )
                            .pointerInput(interactionSource) {
                                detectTapGestures(
                                    onPress = { offset ->
                                        val press = PressInteraction.Press(offset)
                                        interactionSource.emit(press)

                                        send()

                                        val job = coroutineScope.launch {
                                            delay(500)
                                            while (true) {
                                                send()
                                                delay(66)
                                            }
                                        }

                                        tryAwaitRelease()
                                        job.cancel()

                                        interactionSource.emit(PressInteraction.Release(press))
                                    }
                                )
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isDeleting) {
                            Icon(
                                Icons.Filled.Clear,
                                contentDescription = "Delete",
                                modifier = Modifier.size(ButtonDefaults.IconSize),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        } else {
                            Text(
                                text = "$it",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
            }
        }

        item {
            bottomContent()
        }
    }
}