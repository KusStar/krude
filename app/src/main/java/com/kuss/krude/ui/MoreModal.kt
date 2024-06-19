package com.kuss.krude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HideSource
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.krude.R
import com.kuss.krude.ui.components.SettingSections
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.SettingsViewModel

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
            if (settingsState.devExtension && settingsState.devExtensionRepo.isNotEmpty() && settingsState.enableExtension) {
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
                SettingSections(mainViewModel, settingsViewModel, dismiss = { dismiss() })

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

    StarListModal(mainViewModel, visible = showStarTable) {
        showStarTable = false
    }

    HiddenListModal(mainViewModel, visible = showHiddenTable) {
        showHiddenTable = false
    }
}