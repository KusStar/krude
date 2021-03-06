package com.kuss.krude.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import com.kuss.krude.R
import com.kuss.krude.data.AppInfo
import me.zhanghai.android.appiconloader.AppIconLoader

object AppHelper {
    @JvmStatic
    fun getInstalled(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val allApps = pm.getInstalledApplications(0)
        val validApps: MutableList<AppInfo> = ArrayList()
        for (app in allApps) {
            if (pm.getLaunchIntentForPackage(app.packageName) == null) continue
            validApps.add(getAppInfo(app, pm, context))
        }
        return FilterHelper.getSorted(validApps)
    }

    fun getAppInfo(app: ApplicationInfo, pm: PackageManager, context: Context): AppInfo {
        val label = app.loadLabel(pm).toString()
        val packageName = app.packageName
        val iconSize = context.resources.getDimensionPixelSize(R.dimen.app_icon_size)
        val icon = AppIconLoader(iconSize, true, context).loadIcon(app)
        val filterTarget = FilterHelper.toTarget(label, packageName)
        return AppInfo(label, packageName, icon, filterTarget)
    }

}