package com.kuss.krude.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sd.lib.compose.wheel_picker.FHorizontalWheelPicker
import com.sd.lib.compose.wheel_picker.FWheelPickerState
import com.sd.lib.compose.wheel_picker.rememberFWheelPickerState

class ScrollWheelState(val wheelPickerState: FWheelPickerState)

@Composable
fun rememberScrollWheelState(): ScrollWheelState {
    val wheelState = rememberFWheelPickerState()
    val state = ScrollWheelState(wheelState)
    return state
}

@Composable
fun ScrollWheel(count: Int, state: ScrollWheelState) {
    val hapticFeedback = LocalHapticFeedback.current
    val wheelPickerState = state.wheelPickerState
    var hapticFeedbackEnable by remember { mutableStateOf(false) }
    LaunchedEffect(count, state) {
        wheelPickerState.animateScrollToIndex(0)
    }
    LaunchedEffect(wheelPickerState) {
        snapshotFlow { wheelPickerState.currentIndexSnapshot }.collect {
            if (wheelPickerState.currentIndexSnapshot > 0) {
                hapticFeedbackEnable = true
            }
            if (hapticFeedbackEnable) {
                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }
    FHorizontalWheelPicker(state = wheelPickerState,
        unfocusedCount = 4,
        modifier = Modifier.height(24.dp),
        count = count,
        focus = {}
    ) {
        VerticalDivider(color = MaterialTheme.colorScheme.primary, thickness = 2.dp)
    }
}

@Preview
@Composable
fun PreviewScrollWheel() {
    ScrollWheel(count = 20, state = rememberScrollWheelState())
}