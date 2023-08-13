package com.kuss.krude.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.kuss.krude.data.AppInfo

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

    @JvmStatic
    fun getAppInfo(app: ApplicationInfo, pm: PackageManager, context: Context): AppInfo {
        val label = app.loadLabel(pm).toString()
        val abbr = FilterHelper.getAbbr(label)
        val packageName = app.packageName
        val filterTarget = FilterHelper.toTarget(label, packageName)
        return AppInfo(
            label = label,
            abbr = abbr,
            packageName = packageName,
            filterTarget = filterTarget
        )
    }

}