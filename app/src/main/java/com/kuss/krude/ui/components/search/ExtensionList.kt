package com.kuss.krude.ui.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.ui.components.JoystickDirection
import com.kuss.krude.ui.components.JoystickOffsetState
import com.kuss.krude.utils.SizeConst
import com.kuss.krude.utils.measureMaxWidthOfTexts
import com.sd.lib.compose.wheel_picker.FVerticalWheelPicker
import com.sd.lib.compose.wheel_picker.rememberFWheelPickerState

@Composable
fun ExtensionList(
    joystickOffsetState: JoystickOffsetState,
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean,
    groupLayout: Boolean,
) {
    if (groupLayout) {
        ExtensionGroupList(
            joystickOffsetState = joystickOffsetState,
            searchResult = searchResult,
            listState = listState,
            starSet = starSet,
            showUsageCount = showUsageCount,
            onExtensionClick = onExtensionClick,
            reverseLayout = reverseLayout
        )
    } else {
        ExtensionFlatList(
            searchResult = searchResult,
            listState = listState,
            starSet = starSet,
            showUsageCount = showUsageCount,
            onExtensionClick = onExtensionClick,
            reverseLayout = reverseLayout
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
    reverseLayout: Boolean
) {
    val extensions = remember(searchResult) {
        searchResult.filter { it.isExtension() }
    }
    AnimatedVisibility(visible = extensions.isNotEmpty()) {
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
                    showIcon = true
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
    joystickOffsetState: JoystickOffsetState,
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean
) {
    val extensionGroups = remember(searchResult) {
        getExtensionGroup(starSet, searchResult)
    }
    val levelOffset = joystickOffsetState.offset
    val wheelIndexIdxMap = remember(extensionGroups) {
        mutableMapOf<Int, Int>()
    }
    LaunchedEffect(levelOffset.x) {
        if (levelOffset.x >= 0 && levelOffset.x < extensionGroups.size) {
            listState.animateScrollToItem(levelOffset.x)
        } else {
            if (levelOffset.x < 0) {
                listState.animateScrollToItem(extensionGroups.size - 1)
                joystickOffsetState.changeOffset(IntOffset(extensionGroups.size - 1, 0))
            }
            if (levelOffset.x >= extensionGroups.size) {
                listState.animateScrollToItem(0)
                joystickOffsetState.changeOffset(IntOffset(0, 0))
            }
        }
    }
    AnimatedVisibility(visible = extensionGroups.isNotEmpty()) {
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
                    var levelY by remember {
                        mutableIntStateOf(wheelIndexIdxMap.getOrPut(index) { 0 })
                    }
                    LaunchedEffect(state) {
                        snapshotFlow { state.currentIndexSnapshot }
                            .collect {
                                if (state.currentIndexSnapshot > 0) {
                                    hapticFeedbackEnable = true
                                }
                                if (hapticFeedbackEnable) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                            }
                    }
                    LaunchedEffect(key1 = levelOffset.y) {
                        if (index == levelOffset.x) {
                            if (joystickOffsetState.direction == JoystickDirection.UP) {
                                levelY += -1
                            } else if (joystickOffsetState.direction == JoystickDirection.DOWN) {
                                levelY += 1
                            }
                        }
                        levelY = levelY.coerceIn(0, extensions.size - 1)
                    }
                    LaunchedEffect(levelY) {
                        state.animateScrollToIndex(levelY)
                        wheelIndexIdxMap[index] = levelY
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
                                showIcon = false, active = index == levelOffset.x && idx == levelY
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
                        active = index == levelOffset.x
                    )
                }

                if (index < extensionGroups.size - 1) {
                    VerticalDivider(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}