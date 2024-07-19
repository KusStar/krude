package com.kuss.krude.ui.components.internal.kill

import android.app.ActivityManager
import android.app.IActivityManager
import android.app.IActivityTaskManager
import android.content.pm.PackageManager
import android.view.Display
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.files.FileHelper
import com.kuss.krude.ui.components.search.AsyncAppIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import timber.log.Timber


const val ShizukuPermissionCode = 2024

fun getActivityTaskManager(): IActivityTaskManager {
    return IActivityTaskManager.Stub.asInterface(
        ShizukuBinderWrapper(
            SystemServiceHelper.getSystemService(
                "activity_task"
            )
        )
    )
}

fun getActivityManager(): IActivityManager {
    return IActivityManager.Stub.asInterface(
        ShizukuBinderWrapper(
            SystemServiceHelper.getSystemService(
                "activity"
            )
        )
    )
}

private const val MAX_TASKS_NUM = 1000

private fun getAllTasks(activityTaskManager: IActivityTaskManager): List<ActivityManager.RunningTaskInfo> {
    runCatching {
        return activityTaskManager.getTasks(MAX_TASKS_NUM)
    }.getOrElse {
        runCatching {
            return activityTaskManager.getTasks(
                MAX_TASKS_NUM, false
            )
        }.getOrElse {
            runCatching {
                return activityTaskManager.getTasks(
                    MAX_TASKS_NUM, false, false, Display.INVALID_DISPLAY
                )
            }.getOrElse {
                return runCatching {
                    return activityTaskManager.getTasks(MAX_TASKS_NUM, false, false)
                }.getOrElse { listOf() }
            }
        }
    }
}

// https://github.com/RikkaApps/Shizuku-API/tree/master?tab=readme-ov-file#request-permission
fun checkShizukuPermission(code: Int): Boolean {
    if (Shizuku.isPreV11()) {
        // Pre-v11 is unsupported
        return false
    }

    if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
        // Granted
        return true
    } else if (Shizuku.shouldShowRequestPermissionRationale()) {
        // Users choose "Deny and don't ask again"
        return false
    } else {
        // Request the permission
        Shizuku.requestPermission(code)
        return false
    }
}

@Composable
fun KillExtension(focusRequester: FocusRequester) {
    val scope = rememberCoroutineScope()
    var allRunningApps by remember {
        mutableStateOf<List<ActivityManager.RunningAppProcessInfo>>(listOf())
    }
    var hasShizukuBinder by remember {
        mutableStateOf(false)
    }
    var hasShizukuPermission by remember {
        mutableStateOf(false)
    }
    val onRequestPermissionsResult =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            Timber.d("Shizuku.onRequestPermissionsResult: $granted")
            hasShizukuPermission = granted
        }
    val onBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        Timber.d("Shizuku.OnBinderReceivedListener: on received")
        hasShizukuBinder = true
    }
    val onBinderDeadListener = Shizuku.OnBinderDeadListener {
        Timber.d("Shizuku.OnBinderDeadListener: on binder dead")
        hasShizukuBinder = false
    }
    val lifeCycleOwner = LocalLifecycleOwner.current
    var myActivityManager by remember {
        mutableStateOf<IActivityManager?>(null)
    }
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Timber.d("Lifecycle event: $event")
            if (event == Lifecycle.Event.ON_CREATE) {
                Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
                Shizuku.addBinderDeadListener(onBinderDeadListener)
                Shizuku.addRequestPermissionResultListener(onRequestPermissionsResult);
            }
            if (event == Lifecycle.Event.ON_DESTROY) {
                Shizuku.removeBinderReceivedListener(onBinderReceivedListener)
                Shizuku.removeBinderDeadListener(onBinderDeadListener)
                Shizuku.removeRequestPermissionResultListener(onRequestPermissionsResult);
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        if (checkShizukuPermission(ShizukuPermissionCode)) {
            hasShizukuPermission = true
        }
    }
    LaunchedEffect(hasShizukuPermission, hasShizukuBinder) {
        if (hasShizukuBinder && hasShizukuPermission) {
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
            text = "Kill, has Shizuku permission: $hasShizukuPermission",
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