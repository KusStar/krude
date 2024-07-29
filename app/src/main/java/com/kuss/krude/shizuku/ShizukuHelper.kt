package com.kuss.krude.shizuku

import android.app.ActivityManager
import android.app.IActivityManager
import android.app.IActivityTaskManager
import android.content.Context
import android.content.pm.PackageManager
import android.view.Display
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kuss.krude.shizuku.ShizukuHelper.SHIZUKU_INSTALL_PAGE
import com.kuss.krude.shizuku.ShizukuHelper.checkShizukuInstalled
import com.kuss.krude.shizuku.ShizukuHelper.checkShizukuPermission
import com.kuss.krude.ui.theme.errorText
import com.kuss.krude.ui.theme.successText
import com.kuss.krude.utils.ActivityHelper
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import timber.log.Timber


class ShizukuState {
    private var _isInstalled by mutableStateOf(false)
    private var _hasBinder by mutableStateOf(false)
    private var _hasPermission by mutableStateOf(false)

    val isInstalled: Boolean
        get() = _isInstalled
    val hasBinder: Boolean
        get() = _hasBinder
    val hasPermission: Boolean
        get() = _hasPermission

    val usable: Boolean
        get() = isInstalled && hasBinder && hasPermission

    fun setInstalled(installed: Boolean) {
        _isInstalled = installed
    }

    fun setHasBinder(has: Boolean) {
        _hasBinder = has
    }

    fun setHasPermission(has: Boolean) {
        _hasPermission = has
    }
}

@Composable
fun rememberShizukuState(): ShizukuState {
    val context = LocalContext.current
    val shizukuState = remember { ShizukuState() }
    val onRequestPermissionsResult =
        Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
            val granted = grantResult == PackageManager.PERMISSION_GRANTED
            Timber.d("Shizuku.onRequestPermissionsResult: $granted")
            shizukuState.setHasPermission(granted)
        }
    val onBinderReceivedListener = Shizuku.OnBinderReceivedListener {
        Timber.d("Shizuku.OnBinderReceivedListener: on received")
        shizukuState.setHasBinder(true)
    }
    val onBinderDeadListener = Shizuku.OnBinderDeadListener {
        Timber.d("Shizuku.OnBinderDeadListener: on binder dead")
        shizukuState.setHasBinder(false)
    }
    val lifeCycleOwner = LocalLifecycleOwner.current

    fun checkStates() {
        if (checkShizukuInstalled(context)) {
            shizukuState.setInstalled(true)
            if (Shizuku.pingBinder()) {
                if (checkShizukuPermission()) {
                    shizukuState.setHasPermission(true)
                }
            } else {
                shizukuState.setHasBinder(false)
            }
        }
    }

    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Timber.d("Lifecycle event: $event")
            if (event == Lifecycle.Event.ON_CREATE) {
                Shizuku.addBinderReceivedListenerSticky(onBinderReceivedListener)
                Shizuku.addBinderDeadListener(onBinderDeadListener)
                Shizuku.addRequestPermissionResultListener(onRequestPermissionsResult);
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                checkStates()
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
    return shizukuState
}


@Composable
fun ShizukuStatusChecklist(shizukuState: ShizukuState) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    Column {
        StatusItem(
            text = "Shizuku installed",
            enabled = shizukuState.isInstalled,
            toEnableText = "To install"
        ) {
            uriHandler.openUri(SHIZUKU_INSTALL_PAGE)
        }
        if (shizukuState.isInstalled) {
            StatusItem(
                text = "Shizuku connected",
                enabled = shizukuState.hasBinder,
                toEnableText = "To connect"
            ) {
                context.packageManager.getLaunchIntentForPackage(ShizukuHelper.SHIZUKU_PACKAGE_NAME)
                    ?.let { ActivityHelper.startIntentWithTransition(context, it) }
            }
            if (shizukuState.hasBinder) {
                StatusItem(
                    text = "Shizuku permission",
                    enabled = shizukuState.hasPermission,
                    toEnableText = "To grant"
                ) {
                    if (checkShizukuPermission()) {
                        shizukuState.setHasPermission(true)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusItem(text: String, enabled: Boolean, toEnableText: String, toEnable: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp)) {
        Text(text = text, color = MaterialTheme.colorScheme.secondary)
        Spacer(modifier = Modifier.weight(1f))
        if (enabled) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "done",
                tint = MaterialTheme.colorScheme.successText
            )
        } else {
            TextButton(onClick = {
                toEnable()
            }) {
                Text(text = toEnableText)
            }
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "done",
                tint = MaterialTheme.colorScheme.errorText
            )
        }
    }
}

object ShizukuHelper {

    private const val MAX_TASKS_NUM = 1000
    private const val REQUEST_CODE = 2024
    const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
    const val SHIZUKU_INSTALL_PAGE = "https://shizuku.rikka.app/introduction/"

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


    fun getAllTasks(activityTaskManager: IActivityTaskManager): List<ActivityManager.RunningTaskInfo> {
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
    fun checkShizukuPermission(): Boolean {
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
            Shizuku.requestPermission(REQUEST_CODE)
            return false
        }
    }

    fun checkShizukuInstalled(context: Context): Boolean {
        return context.packageManager.getInstalledPackages(0)
            .find { it.packageName == SHIZUKU_PACKAGE_NAME } != null
    }

}