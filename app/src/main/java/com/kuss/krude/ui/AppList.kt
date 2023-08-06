package com.kuss.krude.ui

import android.content.Intent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.kuss.krude.data.AppInfoWithIcon
import com.kuss.krude.utils.AppHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppList() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var items by remember {
        mutableStateOf(listOf<AppInfoWithIcon>())
    }
    var filteredItems by remember {
        mutableStateOf(listOf<AppInfoWithIcon>())
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            items = AppHelper.getInstalled(context)
        }
    }

    val headers by remember {
        derivedStateOf {
            items.map { it.abbr.first().uppercase() }.toSet().toList().sorted()
        }
    }
    var selectedHeaderIndex by remember { mutableIntStateOf(0) }

    var filtering by remember { mutableStateOf("") }

    var showAppDetailSheet by remember { mutableStateOf(false) }
    var selectedDetailApp by remember { mutableStateOf<AppInfoWithIcon?>(null) }
    val focusManager = LocalFocusManager.current

    val listState = rememberLazyStaggeredGridState()

    val firstVisibleItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    val offsets = remember { mutableStateMapOf<Int, Float>() }

    fun openApp(packageName: String) {
        val intent = context
            .packageManager.getLaunchIntentForPackage(packageName)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: return

        ActivityCompat.startActivity(
            context,
            intent,
            null,
        )

        filtering = ""
    }


    fun toAppDetail(item: AppInfoWithIcon) {
        selectedDetailApp = item
        showAppDetailSheet = true

        focusManager.clearFocus()
    }

    fun updateSelectedIndexIfNeeded(offset: Float) {
        val index = offsets
            .mapValues { abs(it.value - offset) }
            .entries
            .minByOrNull { it.value }
            ?.key ?: return
        if (selectedHeaderIndex == index) return
        selectedHeaderIndex = index
        val selectedItemIndex = items.indexOfFirst {
            it.abbr.first().uppercase() == headers[selectedHeaderIndex]
        }
        scope.launch {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            listState.scrollToItem(selectedItemIndex)
        }
    }

    Row {
        LaunchedEffect(firstVisibleItemIndex) {
            val next = headers.indexOfFirst {
                it == items[firstVisibleItemIndex + 1].abbr.first().uppercase()
            }

            selectedHeaderIndex = next
        }

        Column(modifier = Modifier.weight(1f)) {
            LazyVerticalStaggeredGrid(
                state = listState,
                columns = StaggeredGridCells.Adaptive(128.dp),
                // content padding
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(
                    start = 12.dp,
                    top = 16.dp,
                    end = 12.dp,
                    bottom = 12.dp
                ),
                content = {
                    if (items.isNotEmpty()) {
                        items(items.size) { index ->
                            val item = items[index]

                            AppItem(item = item,
                                modifier = Modifier.animateItemPlacement(),
                                onClick = {
                                    openApp(item.packageName)
                                }, onLongClick = {
                                    toAppDetail(item)
                                })
                        }
                    } else {
                        items(16) {
                            AppItemShimmer(Modifier.animateItemPlacement())
                        }
                    }
                }
            )

            BottomSearchBar(
                filtering = filtering,
                setFiltering = {
                    filtering = it
                },
                items = items,
                filteredItems = filteredItems,
                setFilteredItems = {
                    filteredItems = it
                },
                openApp = {
                    openApp(it)
                },
                toAppDetail = {
                    toAppDetail(it)
                })
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
                        updateSelectedIndexIfNeeded(change.position.y)
                    }
                }
                .padding(4.dp)
        ) {
            headers.forEachIndexed { i, header ->
                val active = i == selectedHeaderIndex
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

        AppDetailModal(showAppDetailSheet, selectedDetailApp, onClose = {
            showAppDetailSheet = false
        })
    }
}
