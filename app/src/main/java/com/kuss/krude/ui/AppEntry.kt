package com.kuss.krude.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuss.krude.db.AppInfo
import com.kuss.krude.ui.components.AppItem
import com.kuss.krude.ui.components.AppItemShimmer
import com.kuss.krude.utils.TAG
import com.kuss.krude.viewmodel.MainViewModel
import kotlin.math.roundToInt


@Composable
fun AppEntry(mainViewModel: MainViewModel = viewModel()) {
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

    fun openApp(appInfo: AppInfo) {
        val intent = context
            .packageManager.getLaunchIntentForPackage(appInfo.packageName)
            ?: return

        ActivityCompat.startActivity(
            context,
            intent,
            null
        )

        mainViewModel.setFiltering("")

        mainViewModel.recordOpenApp(context, appInfo)
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
            val minColumnWidth: Float = 100f
            val maxColumnWidth: Float = 500f
            var scale by remember { mutableFloatStateOf(1f) }
            val minScale = minColumnWidth / maxColumnWidth

            val onGesture = { _: Offset, _: Offset, zoom: Float, _: Float ->
                scale = (scale * zoom).coerceIn(minScale, 1f)
                Log.i(TAG, "detectTransformGestures $scale")
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, rotation ->
                            onGesture(centroid, pan, zoom, rotation)
                        }
                    }

            ) {
                val gridState: Float by remember(scale) { derivedStateOf { maxColumnWidth / scale } }
                val numColumns: Int = (gridState / minColumnWidth).roundToInt().coerceAtLeast(1)
                LazyVerticalGrid(columns = GridCells.Fixed(numColumns)) {
                    if (apps.isNotEmpty()) {
                        items(apps.size) { index ->
                            val item = apps[index]
                            AppItem(
                                item = item,
                                onClick = {
                                    openApp(item)
                                })
                        }
                    } else {
                        items(16) {
                            AppItemShimmer()
                        }
                    }
                }
            }
//            LazyVerticalStaggeredGrid(
//                state = listState,
//                columns = StaggeredGridCells.Adaptive(128.dp),
//                // content padding
//                modifier = Modifier.weight(1f),
//                contentPadding = PaddingValues(
//                    start = 12.dp,
//                    top = 16.dp,
//                    end = 12.dp,
//                    bottom = 12.dp
//                ),
//                content = {
//                    if (apps.isNotEmpty()) {
//                        items(apps.size) { index ->
//                            val item = apps[index]
//
//                            AppItem(item = item,
//                                onClick = {
//                                    openApp(item)
//                                }, onLongClick = {
//                                    toAppDetail(item)
//                                })
//                        }
//                    } else {
//                        items(16) {
//                            AppItemShimmer()
//                        }
//                    }
//                }
//            )

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
