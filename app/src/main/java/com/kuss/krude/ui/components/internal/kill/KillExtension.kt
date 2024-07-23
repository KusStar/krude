package com.kuss.krude.ui.components.internal.kill

import android.app.ActivityManager
import android.app.IActivityManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.files.FileHelper
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.shizuku.ShizukuHelper.SHIZUKU_INSTALL_PAGE
import com.kuss.krude.shizuku.ShizukuHelper.checkShizukuPermission
import com.kuss.krude.shizuku.ShizukuHelper.getActivityManager
import com.kuss.krude.shizuku.ShizukuHelper.getActivityTaskManager
import com.kuss.krude.shizuku.ShizukuHelper.getAllTasks
import com.kuss.krude.shizuku.rememberShizukuState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

const val LOADING_INTERVAL = 1000L

@Composable
fun KillExtension(focusRequester: FocusRequester) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember {
        mutableStateOf(true)
    }
    var allRunningApps by remember {
        mutableStateOf<List<ActivityManager.RunningAppProcessInfo>>(listOf())
    }
    var myActivityManager by remember {
        mutableStateOf<IActivityManager?>(null)
    }
    val shizukuState = rememberShizukuState()
    fun loadRunningApps() {
        val activityTaskManager = getActivityTaskManager()
        val runningTaskSet = mutableSetOf<String>()
        getAllTasks(activityTaskManager).forEach {
            it.baseActivity?.let { baseActivity -> runningTaskSet.add(baseActivity.packageName) }
        }
        val processes = myActivityManager?.runningAppProcesses
        if (processes != null) {
            allRunningApps = processes.filter {
                runningTaskSet.contains(it.processName)
            }
            Timber.d("allRunningApps, size = ${allRunningApps.size}")
        }
    }

    fun killProcess(packageName: String) {
        myActivityManager?.forceStopPackage(
            packageName,
            // system user
            0
        )
        loadRunningApps()
    }
    LaunchedEffect(shizukuState.hasPermission, shizukuState.hasBinder) {
        if (shizukuState.hasPermission && shizukuState.hasBinder) {
            myActivityManager = getActivityManager()
            scope.launch {
                withContext(IO) {
                    while (true) {
                        loading = true
                        loadRunningApps()
                        loading = false
                        delay(LOADING_INTERVAL)
                    }
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .heightIn(min = 128.dp)
            .focusRequester(focusRequester),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (shizukuState.isInstalled && shizukuState.hasPermission) {
            LazyColumn(Modifier.padding(bottom = 16.dp)) {
                itemsIndexed(allRunningApps, key = { _, item -> item.pid }) { index, process ->
                    val memoryInfo = remember(process.pid) {
                        myActivityManager?.getProcessMemoryInfo(intArrayOf(process.pid))?.first()
                    }
                    val label = remember {
                        context.packageManager.getApplicationLabel(
                            context.packageManager.getApplicationInfo(
                                process.processName, 0
                            )
                        ).toString()
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncAppIcon(
                            packageName = process.processName, modifier = Modifier.size(48.dp)
                        )
                        Spacing(x = 1)
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    label,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacing(1)
                                Text(
                                    process.processName,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                            if (memoryInfo != null) {
                                val size = FileHelper.formatFileSize(
                                    memoryInfo.totalPss * 1000L
                                )
                                Text(
                                    "$size | ${process.pid}",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {
                            killProcess(process.processName)
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "close",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    if (index < allRunningApps.size - 1) {
                        Spacing(x = 1)
                    }
                }
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(loading, label = "footer") { animLoading ->
                    if (animLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "${allRunningApps.size} apps running",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        } else {
            val uriHandler = LocalUriHandler.current
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Shizuku installed", color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.weight(1f))
                    if (shizukuState.isInstalled) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "done")
                    } else {
                        Button(onClick = {
                            uriHandler.openUri(SHIZUKU_INSTALL_PAGE)
                        }) {
                            Text(text = "To install")
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Shizuku granted", color = MaterialTheme.colorScheme.secondary)
                    Spacer(modifier = Modifier.weight(1f))
                    if (shizukuState.hasPermission) {
                        Icon(imageVector = Icons.Default.Done, contentDescription = "done")
                    } else {
                        Button(onClick = {
                            if (checkShizukuPermission()) {
                                shizukuState.setHasPermission(true)
                            }
                        }) {
                            Text(text = "To grant")
                        }
                    }
                }
            }
        }
    }
}