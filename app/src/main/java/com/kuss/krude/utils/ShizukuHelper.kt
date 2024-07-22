package com.kuss.krude.utils

import android.app.ActivityManager
import android.app.IActivityManager
import android.app.IActivityTaskManager
import android.content.Context
import android.content.pm.PackageManager
import android.view.Display
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kuss.krude.utils.ShizukuHelper.checkShizukuInstalled
import com.kuss.krude.utils.ShizukuHelper.checkShizukuPermission
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

    LaunchedEffect(Unit) {
        if (checkShizukuInstalled(context)) {
            shizukuState.setInstalled(true)
            if (checkShizukuPermission()) {
                shizukuState.setHasPermission(true)
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

object ShizukuHelper {

    private const val MAX_TASKS_NUM = 1000
    private const val REQUEST_CODE = 2024
    private const val SHIZUKU_PACKAGE_NAME = "moe.shizuku.privileged.api"
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