package com.kuss.krude.ui

import android.content.ComponentName
import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.Collections

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppStatsModal(mainViewModel: MainViewModel) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    val uiState by mainViewModel.appStatsModalState.collectAsState()
    val showAppDetailSheet = uiState.visible
    val selectedDetailApp = uiState.appInfo

    if (showAppDetailSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                mainViewModel.setShowAppStatsModal(false)
            },
            sheetState = sheetState,
            modifier = ModalSheetModifier
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(16.dp)
            ) {
                selectedDetailApp?.let { app ->
                    val info = context.packageManager.getPackageInfo(
                        app.packageName,
                        PackageManager.GET_ACTIVITIES
                    )

                    val packageName = app.packageName

                    val launcherApps =
                        context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps

                    Timber.d(
                        "launcherApps.hasShortcutHostPermission(): ${launcherApps.hasShortcutHostPermission()}"
                    )
                    launcherApps.hasShortcutHostPermission()

                    val shortcutQuery = LauncherApps.ShortcutQuery()
// Set these flags to match your use case, for static shortcuts only,
// use FLAG_MATCH_MANIFEST on its own.
                    shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST)
                    shortcutQuery.setPackage(packageName)

                    val shortcuts = try {
                        launcherApps.getShortcuts(shortcutQuery, android.os.Process.myUserHandle())
                    } catch (e: SecurityException) {
                        // This exception will be thrown if your app is not the default launcher
                        Collections.emptyList()
                    }

                    if (shortcuts != null) {
                        Timber.d(
                            "null%s", shortcuts.joinToString { it.shortLabel.toString() })
                    }

                    AsyncAppIcon(packageName = info.packageName, modifier = Modifier.size(64.dp))
                    Text(
                        text = app.label,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 16.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding()
                    )
                    Text(
                        text = app.packageName,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding()
                    )
                    // extra info
                    Row {
                        Text(
                            text = info.versionName.toString(),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 14.sp,
                        )
                        val versionCode =
                            info.longVersionCode.toString()
                        Text(
                            text = "($versionCode)",
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontSize = 14.sp,
                        )
                    }
                    Spacing(3)
                    val activitiesListState = rememberLazyListState()
                    if (info.activities?.isNotEmpty() == true) {
                        LazyColumn(
                            state = activitiesListState,
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                            items(info.activities?.filter { it.exported && it.enabled } ?: listOf()) {
                                TextButton(onClick = {
                                    val intent =
                                        context.packageManager.getLaunchIntentForPackage(it.packageName)
                                    if (intent != null) {
                                        try {
                                            intent.component =
                                                ComponentName(it.packageName, it.name)
                                            ActivityHelper.startIntentWithTransition(
                                                context,
                                                intent
                                            )
                                        } catch (e: Exception) {
                                            intent.component = null
                                            ActivityHelper.startIntentWithTransition(
                                                context,
                                                intent
                                            )
                                        }

                                    }
                                }) {
                                    Column {
                                        val label = it.loadLabel(context.packageManager).toString()
                                        if (label != app.label) {
                                            Text(
                                                text = label,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 16.sp,
                                            )
                                        } else {
                                            Text(
                                                text = it.name,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontSize = 16.sp,
                                            )
                                        }
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
            }
        }
    }
}