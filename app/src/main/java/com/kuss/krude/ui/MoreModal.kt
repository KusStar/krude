package com.kuss.krude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.kuss.krude.R
import com.kuss.krude.ui.components.Spacing
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

    if (showAppUsageSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                dismiss()
            },
            sheetState = sheetState,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val autoFocus = useAutoFocus()

                val showUsageCount = useShowUsageCount()

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.reset_app_priority)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.reset_app_priority))
                        }
                    },
                    onClick = {
                        mainViewModel.resetDbAppsPriority(context)
                        dismiss()
                        refresh()
                    })

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.reload_apps)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.reload_apps))
                        }
                    },
                    onClick = {
                        mainViewModel.loadFromPackageManger(context = context)
                        dismiss()
                        refresh()
                    })

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusWeak,
                                contentDescription = stringResource(id = R.string.auto_focus)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.auto_focus), modifier = Modifier.fillMaxWidth())
                            Spacing(x = 1)
                            Checkbox(checked = autoFocus.value, onCheckedChange = {
                                autoFocus.value = it
                            })
                        }
                    },
                    onClick = {
                        autoFocus.value = !autoFocus.value
                    })

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = stringResource(id = R.string.show_usage_count)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.show_usage_count), modifier = Modifier.fillMaxWidth())
                            Spacing(x = 1)
                            Checkbox(checked = showUsageCount.value, onCheckedChange = {
                                showUsageCount.value = it
                            })
                        }
                    },
                    onClick = {
                        showUsageCount.value = !showUsageCount.value
                    })


                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ShowChart,
                                contentDescription = stringResource(id = R.string.app_usage)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.app_usage))
                        }
                    },
                    onClick = {
                        mainViewModel.setShowAppUsageSheet(true)
                        dismiss()
                    })
            }
        }
    }
}