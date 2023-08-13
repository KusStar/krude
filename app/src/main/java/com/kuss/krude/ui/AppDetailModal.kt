package com.kuss.krude.ui

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.data.AppInfo
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppDetailModal(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val uiState by mainViewModel.state.collectAsState()
    val showAppDetailSheet = uiState.showAppDetailSheet
    val selectedDetailApp = uiState.selectedDetailApp

    fun openAppInfo(item: AppInfo) {
        ActivityHelper.toDetail(context, item.packageName)
    }

    fun uninstallApp(item: AppInfo) {
        ActivityHelper.toUninstall(context, item.packageName)
    }

    if (showAppDetailSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                mainViewModel.setShowAppDetailSheet(false)
            },
            sheetState = sheetState,
            dragHandle = {
                AnimatedContent(
                    targetState = sheetState.currentValue,
                    label = "DragHandle",
                    transitionSpec = {
                        scaleIn(
                            initialScale = 0.92f,
                            animationSpec = tween(220, delayMillis = 90)
                        ).togetherWith(
                            fadeOut(animationSpec = tween(90))
                        )
                    }) {
                    when (it) {
                        SheetValue.Expanded -> BottomSheetDefaults.DragHandle(
                            modifier = Modifier.padding(
                                vertical = 20.dp
                            )
                        )

                        else -> BottomSheetDefaults.DragHandle()
                    }
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                selectedDetailApp?.let { app ->
                    val info = context.packageManager.getPackageInfo(
                        app.packageName,
                        PackageManager.GET_ACTIVITIES
                    )

                    AppItem(
                        item = app,
                        iconSize = 64.dp,
                        titleFontSize = 20.sp, subtitleFontSize = 16.sp,
                        enabled = false
                    )
                    // extra info
                    Row {
                        Text(
                            text = info.versionName,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp,
                        )
                        val versionCode =
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toString() else info.versionCode.toString()
                        Text(
                            text = "($versionCode)",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 14.sp,
                        )
                    }
                    Spacing(3)
                    // btns
                    Row {
                        Button(onClick = {
                            openAppInfo(app)
                        }) {
                            val text = stringResource(id = R.string.app_info)
                            Icon(
                                Icons.Filled.Info,
                                contentDescription = text,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacing(1)
                            Text(text = text)
                        }
                        Spacing(2)
                        Button(onClick = {
                            uninstallApp(app)
                        }) {
                            val text = stringResource(id = R.string.uninstall_app)
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = text,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacing(1)
                            Text(text = text)
                        }
                    }

                    Spacing(3)
                    val activitiesListState = rememberLazyListState()
                    if (info.activities.isNotEmpty()) {
                        LazyColumn(state = activitiesListState) {
                            stickyHeader {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            MaterialTheme.colorScheme.background,
                                            RoundedCornerShape(16.dp)
                                        )
                                ) {
                                    TextButton(onClick = {
                                        scope.launch {
                                            activitiesListState.animateScrollToItem(0)
                                        }
                                    }) {
                                        Text(
                                            text = stringResource(id = R.string.all_activities),
                                            fontSize = 16.sp,
                                        )
                                    }
                                }
                            }
                            items(info.activities.filter { it.exported && it.enabled }) {
                                TextButton(onClick = {  }) {
                                    Column {
                                        Text(
                                            text = it.name,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontSize = 16.sp,
                                        )
                                        Text(
                                            text = it.packageName,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontSize = 12.sp,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

//                    Button(onClick = {
//                        scope.launch { sheetState.hide() }.invokeOnCompletion {
//                            if (!sheetState.isVisible) {
//                                showAppDetailSheet = false
//                            }
//                        }
//                    }) {
//                        Text("Close")
//                    }
            }

        }
    }
}