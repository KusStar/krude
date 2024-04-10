package com.kuss.krude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.krude.R
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.utils.useAutoFocus
import com.kuss.krude.utils.useShowUsageCount
import com.kuss.krude.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreModal(refresh: () -> Unit, mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val uiState by mainViewModel.state.collectAsState()
    val showAppUsageSheet = uiState.showMoreSheet

    fun dismiss() {
        mainViewModel.setShowMoreSheet(false)
    }

    val showAbout = remember {
        mutableStateOf(false)
    }

    if (showAppUsageSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                dismiss()
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                val autoFocus = useAutoFocus()
                val showUsageCount = useShowUsageCount()

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
                    state = autoFocus.value,
                    onCheckedChange = { next ->
                        autoFocus.value = next
                    }
                )

                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = stringResource(id = R.string.show_usage_count)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.show_usage_count)) },
                    state = showUsageCount.value,
                    onCheckedChange = { next ->
                        showUsageCount.value = next
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
                            imageVector = Icons.Default.Info,
                            contentDescription = stringResource(id = R.string.about)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.about)) },
                    onClick = {
                        showAbout.value = true
                    },
                )
            }
        }
        AboutModal(visible = showAbout.value) {
            showAbout.value = false
        }
    }


}