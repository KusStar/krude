package com.kuss.krude.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Segment
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.krude.R
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.ExtensionDisplayModeDefaults
import com.kuss.krude.viewmodel.settings.HoldingHandDefaults
import com.kuss.krude.viewmodel.settings.SettingsViewModel
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModal(
    refresh: () -> Unit,
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val uiState by mainViewModel.state.collectAsState()
    val settingState by settingsViewModel.state.collectAsState()
    val showMoreModal = uiState.showMoreSheet

    fun dismiss() {
        mainViewModel.setShowMoreSheet(false)
    }

    var showAbout by remember {
        mutableStateOf(false)
    }

    var showStarTable by remember {
        mutableStateOf(false)
    }

    if (showMoreModal) {
        ModalBottomSheet(
            onDismissRequest = {
                dismiss()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {

                ProvidePreferenceTheme {
                    val icon = Icons.AutoMirrored.Default.let {
                        return@let if (settingState.holdingHand == HoldingHandDefaults.LEFT) {
                            it.AlignHorizontalLeft
                        } else {
                            it.AlignHorizontalRight
                        }
                    }
                    ListPreference(
                        value = settingState.holdingHand,
                        onValueChange = {
                            settingsViewModel.setHoldingHand(it)
                        },
                        values = listOf(HoldingHandDefaults.LEFT, HoldingHandDefaults.RIGHT),
                        title = { Text(text = "List preference") },
                        modifier = Modifier.fillMaxWidth(),
                        icon = { Icon(imageVector = icon, contentDescription = null) },
                        summary = { Text(text = settingState.holdingHand) }
                    )
                }

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.reset_app_priority)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.reset_app_priority)) },
                    onClick = {
                        mainViewModel.resetDbAppsPriority(context)
                        dismiss()
                        refresh()
                    },
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.reload_apps)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.reload_apps)) },
                    onClick = {
                        mainViewModel.loadFromPackageManger(context = context)
                        dismiss()
                        refresh()
                    },
                )

                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CenterFocusWeak,
                            contentDescription = stringResource(id = R.string.auto_focus)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.auto_focus)) },
                    state = settingState.autoFocus,
                    onCheckedChange = { next ->
                        settingsViewModel.setAutoFocus(next)
                    }
                )

//  extension settings
                OutlinedCard(
                    border = if (settingState.enableExtension) CardDefaults.outlinedCardBorder() else BorderStroke(
                        1.dp,
                        Color.Transparent
                    ),
                ) {
                    SettingsCheckbox(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Extension,
                                contentDescription = stringResource(id = R.string.enable_extension)
                            )
                        },
                        title = { Text(text = stringResource(id = R.string.enable_extension)) },
                        state = settingState.enableExtension,
                        onCheckedChange = { next ->
                            settingsViewModel.setEnableExtension(next)
                        }
                    )

                    AnimatedVisibility(visible = settingState.enableExtension) {
                        Column {
                            ProvidePreferenceTheme {
                                ListPreference(
                                    value = settingState.extensionDisplayMode,
                                    onValueChange = {
                                        settingsViewModel.setExtensionDisplayMode(it)
                                    },
                                    values = listOf(ExtensionDisplayModeDefaults.TOP_OF_APP_LIST, ExtensionDisplayModeDefaults.IN_APP_LIST, ExtensionDisplayModeDefaults.BOTTOM_OF_APP_LIST),
                                    title = { Text(text = stringResource(id = R.string.extension_display_mode)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = { Icon(imageVector = Icons.AutoMirrored.Default.Segment, contentDescription = null) },
                                    summary = { Text(text = settingState.extensionDisplayMode) }
                                )
                            }
                            SettingsMenuLink(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.List,
                                        contentDescription = stringResource(id = R.string.extension_list)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.extension_list)) },
                                onClick = {

                                }
                            )
                        }
                    }
                }
//  extension settings
                if (settingState.enableExtension || settingState.useEmbedKeyboard) {
                    Spacing(x = 1)
                }
//  keyboard settings
                OutlinedCard(
                    border = if (settingState.useEmbedKeyboard) CardDefaults.outlinedCardBorder() else BorderStroke(
                        1.dp,
                        Color.Transparent
                    )
                ) {
                    SettingsCheckbox(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = stringResource(id = R.string.embed_keyboard)
                            )
                        },
                        title = { Text(text = stringResource(id = R.string.embed_keyboard)) },
                        state = settingState.useEmbedKeyboard,
                        onCheckedChange = { next ->
                            settingsViewModel.setUseEmbedKeyboard(next)
                        }
                    )

                    AnimatedVisibility(visible = settingState.useEmbedKeyboard) {
                        Column {
                            SettingsCheckbox(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardBackspace,
                                        contentDescription = stringResource(id = R.string.show_left_side_backspace)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.show_left_side_backspace)) },
                                state = settingState.showLeftSideBackSpace,
                                onCheckedChange = { next ->
                                    settingsViewModel.setShowLeftSideBackspace(next)
                                },
                            )
                            SettingsCheckbox(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.HistoryToggleOff,
                                        contentDescription = stringResource(id = R.string.show_search_history)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.show_search_history)) },
                                state = settingState.showSearchHistory,
                                onCheckedChange = { next ->
                                    settingsViewModel.setShowSearchHistory(next)
                                }
                            )
                        }
                    }
                }

                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = stringResource(id = R.string.show_usage_count)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.show_usage_count)) },
                    state = settingState.showUsageCount,
                    onCheckedChange = { next ->
                        settingsViewModel.setShowUsageCount(next)
                    }
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(id = R.string.app_usage)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.app_usage)) },
                    onClick = {
                        mainViewModel.setShowAppUsageSheet(true)
                        dismiss()
                    },
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(id = R.string.star_table)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.star_table)) },
                    onClick = {
                        showStarTable = true
                        dismiss()
                    },
                )

                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.about)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.about)) },
                    onClick = {
                        showAbout = true
                    },
                )
            }
        }
        AboutModal(visible = showAbout) {
            showAbout = false
        }

    }

    StarTableModal(mainViewModel, visible = showStarTable) {
        showStarTable = false
    }

}