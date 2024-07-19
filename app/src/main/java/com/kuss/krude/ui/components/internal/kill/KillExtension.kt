package com.kuss.krude.ui.components.internal.kill

import android.app.ActivityManager
import android.app.IActivityManager
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.unit.dp
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.files.FileHelper
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.utils.ShizukuHelper.checkShizukuPermission
import com.kuss.krude.utils.ShizukuHelper.getActivityManager
import com.kuss.krude.utils.ShizukuHelper.getActivityTaskManager
import com.kuss.krude.utils.ShizukuHelper.getAllTasks
import com.kuss.krude.utils.rememberShizukuStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun KillExtension(focusRequester: FocusRequester) {
    val scope = rememberCoroutineScope()
    var allRunningApps by remember {
        mutableStateOf<List<ActivityManager.RunningAppProcessInfo>>(listOf())
    }
    var myActivityManager by remember {
        mutableStateOf<IActivityManager?>(null)
    }
    val shizukuState = rememberShizukuStatus()
    LaunchedEffect(Unit) {
        if (checkShizukuPermission()) {
            shizukuState.setHasPermission(true)
        }
    }
    LaunchedEffect(shizukuState.hasPermission, shizukuState.hasBinder) {
        if (shizukuState.hasBinder && shizukuState.hasPermission) {
            myActivityManager = getActivityManager()
            val activityTaskManager = getActivityTaskManager()
            myActivityManager?.let { activityManager ->
                scope.launch {
                    while (true) {
                        val runningTaskSet = mutableSetOf<String>()
                        getAllTasks(activityTaskManager).forEach {
                            it.baseActivity?.let { baseActivity -> runningTaskSet.add(baseActivity.packageName) }
                        }
                        val processes = activityManager.runningAppProcesses
                        allRunningApps = processes.filter {
                            runningTaskSet.contains(it.processName)
                        }
                        Timber.d("allRunningApps, size = ${allRunningApps.size}")
                        delay(1000)
                    }
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .padding(16.dp)
            .heightIn(min = 128.dp)
    ) {
        Text(
            text = "Kill, has Shizuku permission: ${shizukuState.hasPermission}",
            modifier = Modifier.focusRequester(focusRequester)
        )
        val context = LocalContext.current
        LazyColumn {
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
                    AsyncAppIcon(packageName = process.processName, modifier = Modifier.size(48.dp))
                    Spacing(x = 1)
                    Column {
                        Text(
                            label,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )

                        if (memoryInfo != null) {
                            Text(
                                "${process.pid} ${process.processName} | ${
                                    FileHelper.formatFileSize(
                                        memoryInfo.totalPss * 1000L
                                    )
                                }",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                if (index < allRunningApps.size - 1) {
                    Spacing(x = 1)
                }
            }
        }
    }
}