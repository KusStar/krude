package com.kuss.krude.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import kotlin.math.abs


@Composable
fun AlphabetScrollbar(mainViewModel: MainViewModel, listState: LazyStaggeredGridState) {
    val uiState by mainViewModel.state.collectAsState()

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val offsets = remember { mutableStateMapOf<Int, Float>() }

    fun updateSelectedIndexIfNeeded(
        offset: Float,
    ) {
        scope.launch {
            val index = offsets
                .mapValues { abs(it.value - offset) }
                .entries
                .minByOrNull { it.value }
                ?.key ?: return@launch
            if (uiState.selectedHeaderIndex == index) return@launch
            val selectedItemIndex = uiState.items.indexOfFirst {
                it.abbr.first().uppercase() == uiState.headers[index]
            }
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            listState.scrollToItem(selectedItemIndex)
            mainViewModel.setSelectedHeaderIndex(index)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .fillMaxHeight()
            .pointerInput(Unit) {
                detectTapGestures {
                    updateSelectedIndexIfNeeded(it.y)
                }
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, _ ->
                    updateSelectedIndexIfNeeded(
                        change.position.y)
                }
            }
            .padding(8.dp)
    ) {
        uiState.headers.forEachIndexed { i, header ->
            val active = i == uiState.selectedHeaderIndex
            Text(
                header,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .onGloballyPositioned {
                        offsets[i] = it.boundsInParent().center.y
                    }
                    .alpha(if (active) 1f else 0.6f)
            )
        }
    }
}