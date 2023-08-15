package com.kuss.krude.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuss.krude.data.AppInfo
import com.kuss.krude.viewmodel.MainViewModel


@Composable
fun AppList(mainViewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val listState = rememberLazyStaggeredGridState()
    val focusManager = LocalFocusManager.current

    val uiState by mainViewModel.state.collectAsState()
    val apps = uiState.apps
    val scrollbarItems = uiState.scrollbarItems

    val firstVisibleItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }

    LaunchedEffect(Unit) {
        mainViewModel.initPackageEventReceiver(context)

        mainViewModel.loadApps(context)
    }

    fun openApp(packageName: String) {
        val intent = context
            .packageManager.getLaunchIntentForPackage(packageName)
            ?: return

        ActivityCompat.startActivity(
            context,
            intent,
            null,
        )

        mainViewModel.setFiltering("")
    }

    fun toAppDetail(item: AppInfo) {
        mainViewModel.setSelectedDetailApp(item)
        mainViewModel.setShowAppDetailSheet(true)

        focusManager.clearFocus()
    }

    Row {
        LaunchedEffect(firstVisibleItemIndex) {
            val next = scrollbarItems.indexOfFirst {
                it == apps[firstVisibleItemIndex + 1].abbr.first().uppercase()
            }

            mainViewModel.setSelectedHeaderIndex(next)
        }

        Column(modifier = Modifier.weight(1f)) {
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
                        items(apps.size) { index ->
                            val item = apps[index]

                            AppItem(item = item,
                                onClick = {
                                    openApp(item.packageName)
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

            BottomSearchBar(
                mainViewModel,
                openApp = {
                    openApp(it)
                },
                toAppDetail = {
                    toAppDetail(it)
                })
        }

        AlphabetScrollbar(mainViewModel = mainViewModel, listState = listState)

        AppDetailModal(mainViewModel)
    }
}
