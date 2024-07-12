package com.kuss.krude.ui.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.ui.components.ScrollWheelState
import com.kuss.krude.utils.SizeConst
import com.kuss.krude.utils.measureMaxWidthOfTexts
import com.sd.lib.compose.wheel_picker.FVerticalWheelPicker
import com.sd.lib.compose.wheel_picker.rememberFWheelPickerState
import timber.log.Timber

@Composable
fun ExtensionList(
    scrollWheelState: ScrollWheelState,
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean,
    groupLayout: Boolean,
    onStarItem: (extension: Extension) -> Unit
) {
    if (groupLayout) {
        ExtensionGroupList(
            scrollWheelState = scrollWheelState,
            searchResult = searchResult,
            listState = listState,
            starSet = starSet,
            showUsageCount = showUsageCount,
            onExtensionClick = onExtensionClick,
            reverseLayout = reverseLayout,
            onStarItem = onStarItem,
        )
    } else {
        ExtensionFlatList(
            searchResult = searchResult,
            listState = listState,
            starSet = starSet,
            showUsageCount = showUsageCount,
            onExtensionClick = onExtensionClick,
            reverseLayout = reverseLayout,
            onStarItem = onStarItem,
        )
    }
}

@Composable
fun ExtensionFlatList(
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean,
    onStarItem: (extension: Extension) -> Unit,
) {
    val extensions = remember(searchResult) {
        searchResult.filter { it.isExtension() }
    }
    AnimatedVisibility(visible = extensions.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            reverseLayout = reverseLayout
        ) {
            itemsIndexed(extensions, key = { _, item -> item.key() }) { index, item ->
                val extension = item.asExtension()!!
                val isStar = starSet.contains(extension.id)
                ExtensionItem(
                    modifier = Modifier,
                    item = extension,
                    titleFontSize = 14.sp,
                    showStar = isStar,
                    showSubtitle = false,
                    onClick = {
                        onExtensionClick(extension, isStar)
                    },
                    onLongClick = {
                    },
                    showTimes = showUsageCount,
                    showIcon = true,
                    onStarItem = {
                        onStarItem(extension)
                    }
                )
                if (index < extensions.size - 1) {
                    VerticalDivider(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

const val STANDALONE_GROUP = "##standalone##"

data class IExtensionGroupItem(val extensions: List<Extension>, val key: String)

fun getExtensionGroup(
    starSet: Set<String>,
    searchResult: List<SearchResultItem>
): List<IExtensionGroupItem> {
    val searchResultExtensions = searchResult.filter { it.isExtension() }
    val group = searchResultExtensions.map { it.asExtension()!! }.groupBy {
        if (it.required != null) {
            if (it.required!!.size == 1) {
                return@groupBy it.required!![0]
            } else {
                return@groupBy STANDALONE_GROUP
            }
        } else {
            return@groupBy STANDALONE_GROUP
        }
    }
    val standaloneList = group[STANDALONE_GROUP]
    val extensionGroups = group.filter { it.key != STANDALONE_GROUP }.map {
        IExtensionGroupItem(extensions = it.value, key = it.value[0].required!![0])
    }.toMutableList()
    if (standaloneList != null) {
        extensionGroups.addAll(standaloneList.map {
            IExtensionGroupItem(extensions = listOf(it), key = it.id)
        })
    }
    return extensionGroups.sortedByDescending { starSet.contains(it.extensions[0].id) }
}

@Composable
fun ExtensionGroupList(
    scrollWheelState: ScrollWheelState,
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean,
    onStarItem: (extension: Extension) -> Unit,
) {
    val extensionGroups = remember(searchResult) {
        getExtensionGroup(starSet, searchResult)
    }
    val wheelPickerState = scrollWheelState.wheelPickerState
    val wheelIndexToExtension = remember {
        mutableListOf<Pair<Int, Int>>()
    }
    LaunchedEffect(extensionGroups) {
        if (extensionGroups.isEmpty()) {
            return@LaunchedEffect
        }
        wheelIndexToExtension.clear()
        // wheelPickerState.currentIndex -> (groupIndex, extensionIndex)
        // 0 -> (0, 0)
        // 1 -> (0, 1)
        // 2 -> (0, 2)
        // 3 -> (1, 0)
        val out = mutableListOf<Pair<Int, Int>>()
        extensionGroups.forEachIndexed { index, group ->
            group.extensions.forEachIndexed { extensionIndex, _ ->
                out.add(Pair(index, extensionIndex))
            }
        }
        Timber.d(
            "ExtensionsList: wheelIndexToExtension: ${
                out.mapIndexed { index, pair -> "$index-${pair.first}-${pair.second}" }
                    .joinToString(", ")
            }, extensionGroups: ${extensionGroups.joinToString { groupItem -> groupItem.extensions.joinToString { it.name } }}"
        )
        wheelIndexToExtension.addAll(out)
    }
    var pair by remember { mutableStateOf(Pair(-1, -1)) }
    LaunchedEffect(wheelPickerState) {
        snapshotFlow {
            wheelPickerState.currentIndexSnapshot
        }.collect { currentIndexSnapshot ->
            if (currentIndexSnapshot in wheelIndexToExtension.indices) {
                Timber.d("ExtensionsList: wheelPickerState: currentIndexSnapshot: $currentIndexSnapshot, pair: ${wheelIndexToExtension[currentIndexSnapshot]}, $wheelIndexToExtension")
                wheelIndexToExtension[currentIndexSnapshot].let {
                    pair = it
                    listState.animateScrollToItem(pair.first)
                }
            } else {
                pair = Pair(-1, -1)
            }
        }
    }
    AnimatedVisibility(visible = extensionGroups.isNotEmpty(), enter = fadeIn(), exit = fadeOut()) {
        val density = LocalDensity.current
        val hapticFeedback = LocalHapticFeedback.current
        val textMeasurer = rememberTextMeasurer()
        LazyRow(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            reverseLayout = reverseLayout,
        ) {
            itemsIndexed(extensionGroups, key = { _, item -> item.key }) { index, group ->
                val extensions = group.extensions
                if (extensions.size > 1) {
                    val state = rememberFWheelPickerState()
                    var hapticFeedbackEnable by remember { mutableStateOf(false) }
                    val isStar = remember(starSet, state.currentIndex, extensions) {
                        if (state.currentIndex >= 0) starSet.contains(extensions[state.currentIndex].id) else false
                    }
                    LaunchedEffect(state) {
                        snapshotFlow { state.currentIndexSnapshot }
                            .collect { currentIndexSnapshot ->
                                if (currentIndexSnapshot > 0) {
                                    hapticFeedbackEnable = true
                                }
                                if (hapticFeedbackEnable) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                    }
                    LaunchedEffect(pair) {
                        if (pair.first == index) {
                            state.animateScrollToIndex(pair.second)
                        }
                    }
                    val allTexts = remember {
                        extensions.map { it.name }
                    }
                    val maxTextWidth = remember(allTexts) {
                        with(density) {
                            measureMaxWidthOfTexts(
                                textMeasurer,
                                texts = allTexts,
                                style = TextStyle(fontSize = SizeConst.SEARCH_RESULT_FONT_SIZE)
                            ).toDp()
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        StarBox(
                            showStar = isStar,
                        ) {
                            AsyncAppIcon(
                                packageName = extensions[0].required!![0],
                                modifier = Modifier.size(SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE)
                            )
                        }
                        FVerticalWheelPicker(
                            state = state,
                            modifier = Modifier.width(SizeConst.SEARCH_RESULT_WHEEL_PICK_EXTRA_WIDTH + maxTextWidth),
                            count = extensions.size,
                            focus = {},
                        ) { idx ->
                            val extension = extensions[idx]
                            ExtensionItem(
                                modifier = Modifier,
                                item = extension,
                                titleFontSize = SizeConst.SEARCH_RESULT_FONT_SIZE,
                                showStar = isStar,
                                showSubtitle = false,
                                onClick = {
                                    onExtensionClick(extension, isStar)
                                },
                                onLongClick = {
                                },
                                showTimes = showUsageCount,
                                padding = 0.dp,
                                showIcon = false,
                                active = index == pair.first && idx == pair.second,
                                onStarItem = {
                                    onStarItem(extension)
                                }
                            )
                        }
                    }
                } else {
                    val extension = extensions[0]
                    val isStar = starSet.contains(extension.id)
                    ExtensionItem(
                        modifier = Modifier,
                        item = extension,
                        titleFontSize = SizeConst.SEARCH_RESULT_FONT_SIZE,
                        showStar = isStar,
                        showSubtitle = false,
                        onClick = {
                            onExtensionClick(extension, isStar)
                        },
                        onLongClick = {
                        },
                        showTimes = showUsageCount,
                        showIcon = true,
                        active = index == pair.first,
                        onStarItem = {
                            onStarItem(extension)
                        }
                    )
                }

                if (index < extensionGroups.size - 1) {
                    VerticalDivider(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}