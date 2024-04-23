package com.kuss.krude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.krude.R
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.SettingViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModal(refresh: () -> Unit, mainViewModel: MainViewModel, settingViewModel: SettingViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val uiState by mainViewModel.state.collectAsState()
    val settingState by settingViewModel.state.collectAsState()
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

    var showReload by remember {
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
            ) {
                val autoFocus = settingState.autoFocus
                val showUsageCount = settingState.showUsageCount
                val embedKeyboard = settingState.embedKeyboard
                val showSearchHistory = settingState.showSearchHistory

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
                    state = autoFocus,
                    onCheckedChange = { next ->
                        settingViewModel.setAutoFocus(next)
                    }
                )

                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = stringResource(id = R.string.embed_keyboard)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.embed_keyboard)) },
                    state = embedKeyboard,
                    onCheckedChange = { next ->
                        settingViewModel.setEmbedKeyboard(next)
                        showReload = true
                    }
                )

                if (embedKeyboard) {
                    SettingsCheckbox(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.HistoryToggleOff,
                                contentDescription = stringResource(id = R.string.show_search_history)
                            )
                        },
                        title = { Text(text = stringResource(id = R.string.show_search_history)) },
                        state = showSearchHistory,
                        onCheckedChange = { next ->
                            settingViewModel.setShowSearchHistory(next)
                        }
                    )
                }

                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = stringResource(id = R.string.show_usage_count)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.show_usage_count)) },
                    state = showUsageCount,
                    onCheckedChange = { next ->
                        settingViewModel.setShowUsageCount(next)
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

    if (showReload) {
        AlertDialog(
            title = {
                Text(text = stringResource(id = R.string.edit_saved))
            },
            text = {
                Text(text = stringResource(id = R.string.restart_to_apply))
            },
            onDismissRequest = {
                showReload = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ActivityHelper.reloadApp(context)
                    }
                ) {
                    Text(stringResource(id = R.string.restart))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showReload = false
                    }
                ) {
                    Text(stringResource(id = R.string.close))
                }
            }
        )
    }
}