package com.kuss.krude.shizuku

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import com.kuss.krude.BuildConfig
import com.kuss.krude.ui.components.internal.files.FileHelper
import com.kuss.krude.utils.ActivityHelper
import rikka.shizuku.Shizuku
import rikka.shizuku.Shizuku.UserServiceArgs
import timber.log.Timber

object FileExplorerServiceManager {
    var isBind = false

    private val USER_SERVICE_ARGS: UserServiceArgs = UserServiceArgs(
        ComponentName(
            ActivityHelper.getActivity()?.packageName ?: "com.kuss.krude",
            FileExplorerService::class.java.name
        )
    ).daemon(false).debuggable(BuildConfig.DEBUG).processNameSuffix("file_explorer_service")
        .version(1)

    private val SERVICE_CONNECTION: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Timber.d("onServiceConnected: ")
            isBind = true
            FileHelper.setIFileExplorerService(IFileExplorerService.Stub.asInterface(service))
        }

        override fun onServiceDisconnected(name: ComponentName) {
            Timber.d("onServiceDisconnected: ")
            isBind = false
            FileHelper.setIFileExplorerService(null)
        }
    }

    fun bindService() {
        Timber.d("bindService: isBind = $isBind")
        if (!isBind) {
            Shizuku.bindUserService(USER_SERVICE_ARGS, SERVICE_CONNECTION)
        }
    }
}