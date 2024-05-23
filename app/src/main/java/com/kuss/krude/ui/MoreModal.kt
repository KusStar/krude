package com.kuss.krude.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Segment
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.ImportantDevices
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Merge
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
import androidx.compose.runtime.LaunchedEffect
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
import com.kuss.krude.viewmodel.settings.DominantHandDefaults
import com.kuss.krude.viewmodel.settings.ExtensionDisplayModeDefaults
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
    val uiState by mainViewModel.state.collectAsState()
    val showMoreModal = uiState.showMoreSheet
    var showStarTable by remember {
        mutableStateOf(false)
    }

    var showHiddenTable by remember {
        mutableStateOf(false)
    }
    if (showMoreModal) {
        val context = LocalContext.current
        val sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        val settingsState by settingsViewModel.state.collectAsState()

        fun dismiss() {
            mainViewModel.setShowMoreSheet(false)
        }

        var showAbout by remember {
            mutableStateOf(false)
        }

        LaunchedEffect(settingsState.devExtension, settingsState.devExtensionRepo) {
            if (settingsState.enableExtension) {
                mainViewModel.loadExtensions(context)
            }
        }

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
                    .verticalScroll(rememberScrollState()),
            ) {
                ProvidePreferenceTheme {
                    ListPreference(
                        value = settingsState.dominantHand,
                        onValueChange = {
                            settingsViewModel.setDominantHand(it)
                        },
                        values = listOf(DominantHandDefaults.LEFT, DominantHandDefaults.RIGHT),
                        title = { Text(text = stringResource(id = R.string.dominant_hand)) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(imageVector = Icons.AutoMirrored.Default.let {
                                return@let if (settingsState.dominantHand == DominantHandDefaults.LEFT) {
                                    it.AlignHorizontalLeft
                                } else {
                                    it.AlignHorizontalRight
                                }
                            }, contentDescription = null)
                        },
                        summary = { Text(text = settingsState.dominantHand) }
                    )
                }

                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CenterFocusWeak,
                            contentDescription = stringResource(id = R.string.auto_focus)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.auto_focus)) },
                    state = settingsState.autoFocus,
                    onCheckedChange = { next ->
                        settingsViewModel.setAutoFocus(next)
                    }
                )

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
                    },
                )

//  extension settings
                OutlinedCard(
                    border = if (settingsState.enableExtension) CardDefaults.outlinedCardBorder() else BorderStroke(
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
                        state = settingsState.enableExtension,
                        onCheckedChange = { next ->
                            settingsViewModel.setEnableExtension(next)
                        }
                    )

                    AnimatedVisibility(visible = settingsState.enableExtension) {
                        Column {
                            ProvidePreferenceTheme {
                                ListPreference(
                                    value = settingsState.extensionDisplayMode,
                                    onValueChange = {
                                        settingsViewModel.setExtensionDisplayMode(it)
                                    },
                                    values = listOf(
                                        ExtensionDisplayModeDefaults.ON_TOP,
                                        ExtensionDisplayModeDefaults.ON_BOTTOM
                                    ),
                                    title = { Text(text = stringResource(id = R.string.extension_display_mode)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    icon = {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.Segment,
                                            contentDescription = null
                                        )
                                    },
                                    summary = { Text(text = settingsState.extensionDisplayMode) }
                                )
                            }
                            SettingsMenuLink(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = stringResource(id = R.string.refetch_extensions)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.refetch_extensions)) },
                                onClick = {
                                    mainViewModel.clearExtensionsCache(context)
                                    mainViewModel.loadExtensions(context)
                                }
                            )
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
                            SettingsCheckbox(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Merge,
                                        contentDescription = null
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.extension_group_layout)) },
                                state = settingsState.extensionGroupLayout,
                                onCheckedChange = { next ->
                                    settingsViewModel.setExtensionGroupLayout(next)
                                }
                            )
                            if (settingsState.devMode) {
                                SettingsCheckbox(
                                    icon = {
                                        Icon(
                                            imageVector = Icons.Default.ImportantDevices,
                                            contentDescription = null
                                        )
                                    },
                                    title = { Text(text = "Use dev extension repo") },
                                    subtitle = { Text(settingsState.devExtensionRepo) },
                                    state = settingsState.devExtension,
                                    onCheckedChange = { next ->
                                        settingsViewModel.setDevExtension(next)
                                    }
                                )
                            }
                        }
                    }
                }
//  extension settings
                if (settingsState.enableExtension || settingsState.useEmbedKeyboard) {
                    Spacing(x = 1)
                }
//  keyboard settings
                OutlinedCard(
                    border = if (settingsState.useEmbedKeyboard) CardDefaults.outlinedCardBorder() else BorderStroke(
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
                        state = settingsState.useEmbedKeyboard,
                        onCheckedChange = { next ->
                            settingsViewModel.setUseEmbedKeyboard(next)
                        }
                    )

                    AnimatedVisibility(visible = settingsState.useEmbedKeyboard) {
                        Column {
                            SettingsCheckbox(
                                icon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardBackspace,
                                        contentDescription = stringResource(id = R.string.show_left_side_backspace)
                                    )
                                },
                                title = { Text(text = stringResource(id = R.string.show_left_side_backspace)) },
                                state = settingsState.showLeftSideBackSpace,
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
                                state = settingsState.showSearchHistory,
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
                    state = settingsState.showUsageCount,
                    onCheckedChange = { next ->
                        settingsViewModel.setShowUsageCount(next)
                    }
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
                            imageVector = Icons.Default.HideSource,
                            contentDescription = stringResource(id = R.string.hidden_list)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.hidden_list)) },
                    onClick = {
                        showHiddenTable = true
                        dismiss()
                    },
                )

                SettingsMenuLink(
                    modifier = Modifier.padding(bottom = 24.dp),
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
        AboutModal(visible = showAbout, settingsViewModel = settingsViewModel) {
            showAbout = false
        }
    }

    StarTableModal(mainViewModel, visible = showStarTable) {
        showStarTable = false
    }

    HiddenTableModal(mainViewModel, visible = showHiddenTable) {
        showHiddenTable = false
    }
}