package com.kuss.krude.ui

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuss.krude.R
import com.kuss.krude.db.AppInfo
import com.kuss.krude.ui.components.AppItem
import com.kuss.krude.ui.components.AppItemShimmer
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.SettingsViewModel

@Composable
fun AppEntry(
    mainViewModel: MainViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current
    val activity = LocalContext.current as Activity
    val listState = rememberLazyStaggeredGridState()
    val focusManager = LocalFocusManager.current

    val uiState by mainViewModel.state.collectAsState()
    val missingPermission = uiState.missingPermission
    val apps = uiState.apps
    val scrollbarItems = uiState.scrollbarItems

    val firstVisibleItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        mainViewModel.initPackageEventReceiver(context)

        mainViewModel.loadApps(context)
    }

    fun openApp(appInfo: AppInfo) {
        ActivityHelper.startPackageActivity(context, appInfo.packageName, activity.window.decorView)

        mainViewModel.recordOpenApp(context, appInfo)
    }

    fun toAppDetail(item: AppInfo) {
        mainViewModel.setSelectedDetailApp(item)
        mainViewModel.setShowAppDetailSheet(true)

        focusManager.clearFocus()
    }

    if (!missingPermission) {
        Column {
            Row(modifier = Modifier.weight(1f, false)) {
                LaunchedEffect(firstVisibleItemIndex) {
                    val next = scrollbarItems.indexOfFirst {
                        it == apps[firstVisibleItemIndex + 1].abbr.first().uppercase()
                    }

                    mainViewModel.setSelectedHeaderIndex(next)
                }

                LazyVerticalStaggeredGrid(
                    state = listState,
                    columns = StaggeredGridCells.Adaptive(128.dp),
                    // content padding
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        top = 16.dp,
                        end = 12.dp,
                        bottom = 12.dp
                    ),
                    content = {
                        if (apps.isNotEmpty()) {
                            items(apps, key = { item -> item.packageName }) { item ->
                                AppItem(item = item,
                                    onClick = {
                                        openApp(item)
                                    }, onLongClick = {
                                        toAppDetail(item)
                                    })
                            }
                        } else {
                            items(16) {
                                AppItemShimmer()
                            }
                        }
                    }
                )
                AlphabetScrollbar(mainViewModel = mainViewModel, listState = listState)
            }
            BottomSearchBar(
                mainViewModel,
                settingsViewModel,
                openApp = {
                    openApp(it)
                },
                toAppDetail = {
                    toAppDetail(it)
                })
        }
        AppDetailModal(mainViewModel)
    } else {
        AlertDialog(
            title = {
                Text(text = stringResource(id = R.string.missing_permission))
            },
            text = {
                Text(text = stringResource(id = R.string.request_permission))
            },
            onDismissRequest = {
                mainViewModel.loadApps(context)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        ActivityHelper.toDetail(context, context.packageName)
                    }
                ) {
                    Text(stringResource(id = R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        mainViewModel.loadApps(context)
                    }
                ) {
                    Text(stringResource(id = R.string.close))
                }
            }
        )
    }
}
