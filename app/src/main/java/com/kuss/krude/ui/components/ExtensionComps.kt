package com.kuss.krude.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.sd.lib.compose.wheel_picker.FVerticalWheelPicker
import com.sd.lib.compose.wheel_picker.rememberFWheelPickerState
import timber.log.Timber

@Composable
fun ExtensionIcon(iconSize: Dp) {
    Icon(
        Icons.Filled.Extension,
        tint = MaterialTheme.colorScheme.primary,
        contentDescription = "Star",
        modifier = Modifier.size(iconSize)
    )
}

@Composable
fun ExtensionContent(
    item: Extension,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    showTimes: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showStar) {
                Icon(
                    Icons.Filled.Star,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "Star",
                    modifier = Modifier.size(12.dp)
                )
            }
            Text(
                text = item.name,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                fontSize = titleFontSize,
            )
        }

        AnimatedVisibility(visible = showTimes) {
            Spacing(1, 4)
            Text(
                text = "${item.priority}${stringResource(id = R.string.open_times)}",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = subtitleFontSize,
            )
        }

        AnimatedVisibility(visible = showSubtitle && item.description != null) {
            Spacing(1, 4)
            Text(
                text = item.description!!,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.secondary,
                fontSize = subtitleFontSize,
            )
        }
    }
}

@Composable
fun ExtensionItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    item: Extension,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    enabled: Boolean = true,
    iconSize: Dp = 32.dp,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    showTimes: Boolean = false,
    horizontal: Boolean = false,
    padding: Dp = 4.dp
) {
    CustomButton(
        onClick = onClick,
        onLongClick = onLongClick,
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        Timber.d("AppItem", "onLongPress")
                    }
                )
            },
        shape = RoundedCornerShape(8.dp),
        enabled = enabled
    ) {
        if (horizontal) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(padding)
            ) {
                if (!item.required.isNullOrEmpty()) {
                    AsyncAppIcon(packageName = item.required!![0], modifier = Modifier.size(24.dp))
                }
                Spacing(x = 0.5f)
                ExtensionContent(
                    item = item,
                    showStar = showStar,
                    showTimes = showTimes,
                    showSubtitle = showSubtitle,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ExtensionIcon(iconSize)
                Spacing(x = 1)
                ExtensionContent(
                    item = item,
                    showStar = showStar,
                    showTimes = showTimes,
                    showSubtitle = showSubtitle,
                    titleFontSize = titleFontSize,
                    subtitleFontSize = subtitleFontSize
                )
            }
        }
    }
}

@Composable
fun ExtensionList(
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean,
    groupLayout: Boolean
) {
    if (groupLayout) {
        ExtensionGroupList(
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
                val isStar = starSet.contains(extension.name)
                ExtensionItem(
                    modifier = Modifier,
                    item = extension,
                    titleFontSize = 14.sp,
                    showStar = isStar,
                    showSubtitle = false,
                    horizontal = true,
                    onClick = {
                        onExtensionClick(extension, isStar)
                    },
                    onLongClick = {
                    },
                    showTimes = showUsageCount,
                )
                if (index < extensions.size - 1) {
                    VerticalDivider(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

const val STANDALONE_GROUP = "##standalone##"

data class ExtensionGroupItem(val extensions: List<Extension>, val key: String)

@Composable
fun ExtensionGroupList(
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    showUsageCount: Boolean,
    onExtensionClick: (extension: Extension, isStar: Boolean) -> Unit,
    reverseLayout: Boolean
) {
    val searchResultExtensions = remember(searchResult) {
        searchResult.filter { it.isExtension() }
    }
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
    AnimatedVisibility(visible = searchResultExtensions.isNotEmpty()) {
        val hapticFeedback = LocalHapticFeedback.current
        val standaloneList = group[STANDALONE_GROUP]
        val extensionGroups = group.filter { it.key != STANDALONE_GROUP }.map {
            ExtensionGroupItem(extensions = it.value, key = it.value[0].required!![0])
        }.toMutableList()
        if (standaloneList != null) {
            extensionGroups.addAll(standaloneList.map {
                ExtensionGroupItem(extensions = listOf(it), key = it.id)
            })
        }
        LazyRow(
            modifier = Modifier
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            reverseLayout = reverseLayout
        ) {
            itemsIndexed(extensionGroups, key = { _, item -> item.key }) { index, group ->
                val extensions = group.extensions
                if (extensions.size > 1) {
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
                    FVerticalWheelPicker(
                        state = state,
                        modifier = Modifier.width(128.dp),
                        count = extensions.size,
                        itemHeight = 40.dp,
                        focus = {},
                    ) { idx ->
                        val extension = extensions[idx]
                        val isStar = starSet.contains(extension.name)
                        ExtensionItem(
                            modifier = Modifier,
                            item = extension,
                            titleFontSize = 14.sp,
                            showStar = isStar,
                            showSubtitle = false,
                            horizontal = true,
                            onClick = {
                                onExtensionClick(extension, isStar)
                            },
                            onLongClick = {
                            },
                            showTimes = showUsageCount,
                            padding = 0.dp
                        )
                    }
                } else {
                    val extension = extensions[0]
                    val isStar = starSet.contains(extension.name)
                    ExtensionItem(
                        modifier = Modifier,
                        item = extension,
                        titleFontSize = 14.sp,
                        showStar = isStar,
                        showSubtitle = false,
                        horizontal = true,
                        onClick = {
                            onExtensionClick(extension, isStar)
                        },
                        onLongClick = {
                        },
                        showTimes = showUsageCount,
                    )
                }

                if (index < extensionGroups.size - 1) {
                    VerticalDivider(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
