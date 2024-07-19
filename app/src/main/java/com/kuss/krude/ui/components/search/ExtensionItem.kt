package com.kuss.krude.ui.components.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.ui.components.CustomButton
import com.kuss.krude.ui.components.OnOpenInFreeformDropdown
import com.kuss.krude.ui.components.OnStarDropdown
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.InternalExtensionIcon
import com.kuss.krude.utils.SizeConst
import me.saket.cascade.CascadeDropdownMenu
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
    showSubtitle: Boolean = true,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    showTimes: Boolean = false,
    active: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = item.name,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            fontSize = titleFontSize,
            textDecoration = if (active) TextDecoration.Underline else null
        )
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

enum class ExtensionDropdownType {
    STAR,
    OPEN_IN_FREEFORM_WINDOW
}

@Composable
fun ExtensionItem(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onDropdown: (ExtensionDropdownType) -> Unit,
    item: Extension,
    showStar: Boolean = false,
    showSubtitle: Boolean = true,
    showContent: Boolean = true,
    enabled: Boolean = true,
    titleFontSize: TextUnit = 16.sp,
    subtitleFontSize: TextUnit = 12.sp,
    iconSize: Dp = SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE,
    showTimes: Boolean = false,
    padding: Dp = 4.dp,
    showIcon: Boolean,
    active: Boolean = false
) {
    var showDropdown by remember {
        mutableStateOf(false)
    }
    CustomButton(
        onClick = onClick,
        onLongClick = {
            showDropdown = true
            onLongClick()
        },
        modifier = modifier.pointerInput(Unit) {
            detectTapGestures(onLongPress = {
                Timber.d("AppItem", "onLongPress")
            })
        },
        shape = RoundedCornerShape(8.dp),
        enabled = enabled,
    ) {
        Box {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(padding)
            ) {
                if (showIcon) {
                    StarBox(showStar) {
                        if (!item.required.isNullOrEmpty()) {
                            AsyncAppIcon(
                                packageName = item.required!![0], modifier = Modifier.size(iconSize)
                            )
                        } else {
                            if (item.type == ExtensionType.INTERNAL) {
                                InternalExtensionIcon(item, size = iconSize)
                            } else {
                                ExtensionIcon(iconSize = iconSize)
                            }
                        }
                    }
                }
                if (showContent) {
                    Spacing(x = 0.5f)
                    ExtensionContent(
                        item = item,
                        showTimes = showTimes,
                        showSubtitle = showSubtitle,
                        titleFontSize = titleFontSize,
                        subtitleFontSize = subtitleFontSize,
                        active = active
                    )
                }
            }
            CascadeDropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false }) {
                OnStarDropdown {
                    onDropdown(ExtensionDropdownType.STAR)
                    showDropdown = false
                }
                OnOpenInFreeformDropdown {
                    onDropdown(ExtensionDropdownType.OPEN_IN_FREEFORM_WINDOW)
                    showDropdown = false
                }
            }
        }
    }
}


