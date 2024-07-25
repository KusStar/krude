package com.kuss.krude.ui.components.internal.kill

import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.app.IActivityManager
import android.content.Context
import android.os.Debug.MemoryInfo
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.kuss.krude.R
import com.kuss.krude.shizuku.ShizukuHelper.getActivityManager
import com.kuss.krude.shizuku.ShizukuHelper.getActivityTaskManager
import com.kuss.krude.shizuku.ShizukuHelper.getAllTasks
import com.kuss.krude.shizuku.ShizukuStatusChecklist
import com.kuss.krude.shizuku.rememberShizukuState
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.files.FileHelper
import com.kuss.krude.ui.components.search.AsyncAppIcon
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


const val LOADING_INTERVAL = 1000L

data class RunningProcessInfo(
    val runningAppProcessInfo: RunningAppProcessInfo,
    val memoryInfo: MemoryInfo? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun KillExtension(focusRequester: FocusRequester, onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var loading by remember {
        mutableStateOf(true)
    }
    var allRunningProcessInfo by remember {
        mutableStateOf<List<RunningProcessInfo>>(listOf())
    }
    var myActivityManager by remember {
        mutableStateOf<IActivityManager?>(null)
    }
    val shizukuState = rememberShizukuState()
    var showAllProcess by remember {
        mutableStateOf(false)
    }
    val installedPackagesSet = remember {
        context.packageManager.getInstalledPackages(0).map { it.packageName }.toSet()
    }
    var currentMemoryInfo by remember {
        mutableStateOf<ActivityManager.MemoryInfo?>(null)
    }

    fun loadMemoryInfo() {
        val activityManager =
            context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        currentMemoryInfo = memoryInfo
    }

    fun loadRunningProcessInfo() {
        val activityTaskManager = getActivityTaskManager()
        val runningTaskSet = mutableSetOf<String>()
        getAllTasks(activityTaskManager).forEach {
            it.baseActivity?.let { baseActivity -> runningTaskSet.add(baseActivity.packageName) }
        }
        val processes = myActivityManager?.runningAppProcesses

        if (processes != null) {
            allRunningProcessInfo = processes.filter {
                if (showAllProcess) {
                    installedPackagesSet.contains(it.processName)
                } else {
                    runningTaskSet.contains(it.processName)
                }
            }.map { process ->
                val memoryInfo =
                    myActivityManager?.getProcessMemoryInfo(intArrayOf(process.pid))?.first()
                RunningProcessInfo(runningAppProcessInfo = process, memoryInfo = memoryInfo)
            }.sortedByDescending { it.memoryInfo?.totalPss }
            Timber.d("allRunningApps, size = ${allRunningProcessInfo.size}")
            loadMemoryInfo()
        }
    }

    fun killProcess(packageName: String) {
        myActivityManager?.forceStopPackage(
            packageName,
            // system user
            0
        )
        loadRunningProcessInfo()
    }
    LaunchedEffect(shizukuState.hasPermission, shizukuState.hasBinder) {
        if (shizukuState.hasPermission && shizukuState.hasBinder) {
            myActivityManager = getActivityManager()
            scope.launch {
                withContext(IO) {
                    while (true) {
                        loading = true
                        loadRunningProcessInfo()
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                IconButton(onClick = { onBack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "back",
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Text(
                    text = stringResource(id = R.string.kill_running_apps),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                Checkbox(checked = showAllProcess, onCheckedChange = {
                    showAllProcess = it
                    loadRunningProcessInfo()
                })
            }
            Spacing(x = 1)
            LazyColumn {
                stickyHeader {
                    currentMemoryInfo?.let { memoryInfo ->
                        val totalMem = memoryInfo.totalMem
                        val availMem = memoryInfo.availMem
                        val usedMem = totalMem - availMem
                        val availMemoryText = FileHelper.formatFileSize(availMem)
                        val usedMemoryText = FileHelper.formatFileSize(usedMem)
                        val percentage = (usedMem * 100 / totalMem)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "${allRunningProcessInfo.size} running, ${percentage}%, used $usedMemoryText, avail $availMemoryText",
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                }
                itemsIndexed(
                    allRunningProcessInfo,
                    key = { _, item -> item.runningAppProcessInfo.pid }) { index, processInfo ->
                    val process = processInfo.runningAppProcessInfo
                    val memoryInfo = processInfo.memoryInfo
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
                        Column(Modifier.widthIn(max = 256.dp)) {
                            Text(
                                label,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (label != process.processName) {
                                Text(
                                    process.processName,
                                    color = MaterialTheme.colorScheme.secondary,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
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
                    if (index < allRunningProcessInfo.size - 1) {
                        Spacing(x = 1)
                    }
                }
            }

        } else {
            ShizukuStatusChecklist(shizukuState)
        }
    }
}
