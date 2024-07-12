package com.kuss.krude.ui.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuss.krude.db.AppInfo
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.utils.Reverse
import com.kuss.krude.utils.SizeConst
import com.kuss.krude.viewmodel.settings.DominantHandDefaults


@Composable
fun CloseBtn(visible: Boolean, onClick: () -> Unit) {
    AnimatedVisibility(visible = visible) {
        IconButton(onClick = {
            onClick()
        }) {
            Icon(
                Icons.Filled.Clear,
                contentDescription = "Clear",
                modifier = Modifier.size(ButtonDefaults.IconSize),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    }
}


@Composable
fun FastClickBtns(
    visible: Boolean,
    searchResult: List<SearchResultItem>,
    onExtensionClick: (Extension, Boolean) -> Unit,
    onAppClick: (AppInfo, Boolean) -> Unit,
    starSet: Set<String>,
) {
    val firstExtension = searchResult.firstOrNull { it.isExtension() }?.asExtension()
    val firstApp = searchResult.firstOrNull { it.isApp() }?.asApp()
    AnimatedVisibility(visible = visible) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            val iconSize = SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE
            if (firstExtension != null) {
                val isStar = starSet.contains(firstExtension.id)
                IconButton(onClick = {
                    onExtensionClick(firstExtension, isStar)
                }) {
                    StarBox(isStar, isExtension = true) {
                        if (!firstExtension.required.isNullOrEmpty()) {
                            AsyncAppIcon(
                                packageName = firstExtension.required!![0], modifier = Modifier.size(iconSize)
                            )
                        } else {
                            if (firstExtension.type == ExtensionType.INTERNAL) {
                                InternalExtensionIcon(
                                    extension = firstExtension, size = iconSize
                                )
                            } else {
                                ExtensionIcon(iconSize = iconSize)
                            }
                        }
                    }
                }
            }
            if (firstApp != null) {
                val showStar = starSet.contains(firstApp.packageName)
                IconButton(onClick = {
                    onAppClick(firstApp, showStar)
                }) {
                    StarBox(showStar) {
                        AsyncAppIcon(
                            packageName = firstApp.packageName, modifier = Modifier.size(iconSize)
                        )
                    }
                }
            }
            VerticalDivider(Modifier.height(16.dp))
        }
    }
}

@Composable
fun MoreBtns(
    search: String,
    searchResult: List<SearchResultItem>,
    fuzzySearch: Boolean,
    onStarIcon: () -> Unit,
    onFuzzyIcon: () -> Unit,
    onMoreIcon: () -> Unit,
    dominantHand: String,
    onExtensionClick: (Extension, Boolean) -> Unit,
    onAppClick: (AppInfo, Boolean) -> Unit,
    starSet: Set<String>
) {
    Box(
        modifier = Modifier
            .wrapContentSize(Alignment.TopStart)
    ) {
        Row(
            horizontalArrangement =
            if (dominantHand == DominantHandDefaults.LEFT) Arrangement.Start else Arrangement.Reverse,
        ) {
            // TODO: rethinking necessity
//            FastClickBtns(
//                visible = search.isNotEmpty() && searchResult.isNotEmpty(),
//                searchResult = searchResult,
//                onExtensionClick = onExtensionClick,
//                onAppClick = onAppClick,
//                starSet = starSet,
//            )
            AnimatedVisibility(visible = search.isNotEmpty() && searchResult.isNotEmpty()) {
                IconButton(onClick = {
                    onStarIcon()
                }) {
                    Icon(
                        Icons.TwoTone.Star,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "Star",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }
            IconButton(onClick = {
                onFuzzyIcon()
            }) {
                Icon(
                    imageVector = if (fuzzySearch) Icons.Filled.BlurOn else Icons.Filled.BlurOff,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "fuzzysearch",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
            IconButton(onClick = { onMoreIcon() }) {
                Icon(
                    Icons.Filled.MoreVert,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "MoreVert",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
    }
}