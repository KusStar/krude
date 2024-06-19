package com.kuss.krude.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.kuss.krude.ui.theme.errorBackground
import com.kuss.krude.ui.theme.errorText
import com.kuss.krude.ui.theme.successBackground
import com.kuss.krude.ui.theme.successText
import com.kuss.krude.ui.theme.warningBackground
import com.kuss.krude.ui.theme.warningText
import com.kuss.krude.utils.applyIf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.DurationUnit

enum class MessageType {
    DEFAULT,
    ERROR,
    WARNING,
    SUCCESS
}

class MessageBarState(private val scope: CoroutineScope) {
    var isLoading by mutableStateOf(false)
    var isShowing by mutableStateOf(false)
    var message by mutableStateOf("")
    var type by mutableStateOf(MessageType.DEFAULT)
    private var job: Job? = null

    fun show(message: String, type: MessageType = MessageType.DEFAULT, duration: Duration? = null) {
        job?.cancel()
        this.message = message
        this.type = type
        isShowing = true

        if (duration == null) return
        job = scope.launch {
            delay(duration.toLong(DurationUnit.MILLISECONDS))
            dismiss()
        }
    }

    fun showSuccess(message: String, duration: Duration? = null) {
        isLoading = false
        show(message, type = MessageType.SUCCESS, duration)
    }

    fun showError(message: String, duration: Duration? = null) {
        isLoading = false
        show(message, type = MessageType.ERROR, duration)
    }

    fun dismiss() {
        isShowing = false
        isLoading = false
    }

    fun showLoading(message: String, duration: Duration? = null) {
        isLoading = true
        show(message, duration = duration)
    }
}

@Composable
fun rememberMessageBarState(): MessageBarState {
    val scope = rememberCoroutineScope()
    val messageBarState = remember { MessageBarState(scope) }
    return messageBarState
}

@Composable
fun MessageBar(
    state: MessageBarState, modifier: Modifier = Modifier, topContent: @Composable () -> Unit = {
        HorizontalDivider()
    }
) {
    if (state.isShowing) {
        topContent()
    }
    AnimatedVisibility(state.isShowing) {
        Row(
            modifier = modifier
                .applyIf(state.type == MessageType.ERROR) {
                    background(colorScheme.errorBackground, RectangleShape)
                }
                .applyIf(state.type == MessageType.WARNING) {
                    background(colorScheme.warningBackground, RectangleShape)
                }
                .applyIf(state.type == MessageType.SUCCESS) {
                    background(colorScheme.successBackground, RectangleShape)
                }
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 1.5.dp)
            }
            if (state.isLoading) {
                Spacing(1)
            }

            val textColor = when (state.type) {
                MessageType.ERROR -> colorScheme.errorText
                MessageType.WARNING -> colorScheme.warningText
                MessageType.SUCCESS -> colorScheme.successText
                else -> colorScheme.secondary
            }
            val icon = when (state.type) {
                MessageType.ERROR -> Icons.Default.Error
                MessageType.WARNING -> Icons.Default.Warning
                MessageType.SUCCESS -> Icons.Default.Check
                else -> null
            }
            if (state.type != MessageType.DEFAULT) {
                Icon(
                    imageVector = icon!!,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacing(1)
            }
            Text(state.message, color = textColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}