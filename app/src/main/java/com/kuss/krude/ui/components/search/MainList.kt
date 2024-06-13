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
import com.kuss.krude.db.AppInfo
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.utils.SizeConst
import com.kuss.krude.utils.applyIf
import com.kuss.krude.utils.measureMaxWidthOfTexts
import com.kuss.krude.viewmodel.settings.ExtensionDisplayModeDefaults
import com.kuss.krude.viewmodel.settings.SettingsState
import com.sd.lib.compose.wheel_picker.FVerticalWheelPicker
import com.sd.lib.compose.wheel_picker.rememberFWheelPickerState


@Composable
fun MainList(
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    settingsState: SettingsState,
    onAppClick: (app: AppInfo, isStar: Boolean) -> Unit,
    toAppDetail: (AppInfo) -> Unit,
    reverseLayout: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
) {
    val isInline = remember(settingsState.extensionDisplayMode) {
        settingsState.extensionDisplayMode == ExtensionDisplayModeDefaults.IN_LINE
    }
    val mainData = remember(isInline, searchResult) {
        if (isInline) {
            searchResult
        } else {
            searchResult.filter { it.isApp() }
        }
    }
    if (isInline) {
        MainGroupList(
            data = mainData,
            listState = listState,
            starSet = starSet,
            settingsState = settingsState,
            onAppClick = onAppClick,
            toAppDetail = toAppDetail,
            reverseLayout = reverseLayout,
            onExtensionClick = onExtensionClick
        )
    } else {
        AnimatedVisibility(visible = mainData.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .padding(vertical = if (settingsState.appItemHorizontal) 12.dp else 8.dp)
                    .animateContentSize(),
                verticalAlignment = Alignment.CenterVertically,
                state = listState,
                reverseLayout = reverseLayout
            ) {
                itemsIndexed(
                    mainData,
                    key = { _, item -> item.key() }) { index, item ->
                    val app = item.asApp()!!
                    val isStar = starSet.contains(app.packageName)
                    AppItem(
                        modifier = Modifier.applyIf(!settingsState.appItemHorizontal) { width(96.dp) },
                        item = app,
                        titleFontSize = SizeConst.SEARCH_RESULT_FONT_SIZE,
                        showStar = isStar,
                        titleSingleLine = true,
                        showSubtitle = false,
                        onClick = {
                            onAppClick(app, isStar)
                        },
                        onLongClick = {
                            toAppDetail(app)
                        },
                        iconSize = if (settingsState.appItemHorizontal) SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE else SizeConst.SEARCH_RESULT_LARGE_ICON_SIZE,
                        showTimes = settingsState.showUsageCount,
                        horizontal = settingsState.appItemHorizontal
                    )

                    if (settingsState.appItemHorizontal && index < mainData.size - 1) {
                        VerticalDivider(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}


data class IMainGroupItem(val items: List<SearchResultItem>, val key: String)

fun getMainGroupList(data: List<SearchResultItem>): List<IMainGroupItem> {
    val group = data.groupBy {
        if (it.isExtension()) {
            val ext = it.asExtension()!!
            if (ext.required != null) {
                if (ext.required!!.size == 1) {
                    return@groupBy ext.required!![0]
                } else {
                    return@groupBy STANDALONE_GROUP
                }
            } else {
                return@groupBy STANDALONE_GROUP
            }
        } else {
            return@groupBy it.key()
        }
    }
    val standaloneList = group[STANDALONE_GROUP]
    val itemsGroups = group.filter { it.key != STANDALONE_GROUP }.map {
        IMainGroupItem(items = it.value, key = it.key)
    }.toMutableList()
    if (standaloneList != null) {
        itemsGroups.addAll(standaloneList.map {
            IMainGroupItem(items = listOf(it), key = it.key())
        })
    }
    return itemsGroups
}

@Composable
fun MainGroupList(
    data: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    settingsState: SettingsState,
    onAppClick: (app: AppInfo, isStar: Boolean) -> Unit,
    toAppDetail: (AppInfo) -> Unit,
    reverseLayout: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val groupList = getMainGroupList(data)
    AnimatedVisibility(visible = groupList.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .padding(vertical = if (settingsState.appItemHorizontal) 12.dp else 8.dp)
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            reverseLayout = reverseLayout
        ) {
            itemsIndexed(
                groupList,
                key = { _, item -> item.key }) { index, item ->
                val isSingle = item.items.size == 1
                if (isSingle) {
                    val singleItem = item.items[0]
                    if (singleItem.isApp()) {
                        val app = singleItem.asApp()!!
                        val isStar = starSet.contains(app.packageName)
                        AppItem(
                            modifier = Modifier.applyIf(!settingsState.appItemHorizontal) {
                                width(
                                    96.dp
                                )
                            },
                            item = app,
                            titleFontSize = SizeConst.SEARCH_RESULT_FONT_SIZE,
                            showStar = isStar,
                            titleSingleLine = true,
                            showSubtitle = false,
                            onClick = {
                                onAppClick(app, isStar)
                            },
                            onLongClick = {
                                toAppDetail(app)
                            },
                            iconSize = if (settingsState.appItemHorizontal) SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE else SizeConst.SEARCH_RESULT_LARGE_ICON_SIZE,
                            showTimes = settingsState.showUsageCount,
                            horizontal = settingsState.appItemHorizontal
                        )
                    } else if (singleItem.isExtension()) {
                        val extension = singleItem.asExtension()!!
                        val isStar = starSet.contains(extension.name)
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
                            showTimes = settingsState.showUsageCount,
                            showIcon = true
                        )
                    }
                } else {
                    val items = item.items
                    val state = rememberFWheelPickerState()
                    var hapticFeedbackEnable by remember { mutableStateOf(false) }
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
                    val allTexts = remember {
                        items.map {
                            if (it.isApp()) {
                                it.asApp()!!.label
                            } else {
                                it.asExtension()!!.name
                            }
                        }
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
                        AsyncAppIcon(
                            packageName = item.key,
                            modifier = Modifier.size(SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE)
                        )
                        FVerticalWheelPicker(
                            state = state,
                            modifier = Modifier.width(SizeConst.SEARCH_RESULT_WHEEL_PICK_EXTRA_WIDTH + maxTextWidth),
                            count = items.size,
                            focus = {},
                        ) { idx ->
                            val pickerItem = items[idx]
                            if (pickerItem.isApp()) {
                                val app = pickerItem.asApp()!!
                                val isStar = starSet.contains(app.packageName)
                                AppItem(
                                    modifier = Modifier.applyIf(!settingsState.appItemHorizontal) {
                                        width(
                                            96.dp
                                        )
                                    },
                                    item = app,
                                    titleFontSize = SizeConst.SEARCH_RESULT_FONT_SIZE,
                                    showStar = isStar,
                                    titleSingleLine = true,
                                    showSubtitle = false,
                                    onClick = {
                                        onAppClick(app, isStar)
                                    },
                                    onLongClick = {
                                        toAppDetail(app)
                                    },
                                    iconSize = if (settingsState.appItemHorizontal) SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE else SizeConst.SEARCH_RESULT_LARGE_ICON_SIZE,
                                    showTimes = settingsState.showUsageCount,
                                    horizontal = true,
                                    showIcon = false
                                )
                            } else if (pickerItem.isExtension()) {
                                val extension = pickerItem.asExtension()!!
                                val isStar = starSet.contains(extension.name)
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
                                    showTimes = settingsState.showUsageCount,
                                    padding = 0.dp,
                                    showIcon = false,
                                )
                            }
                        }
                    }
                }

                if (settingsState.appItemHorizontal && index < groupList.size - 1) {
                    VerticalDivider(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}