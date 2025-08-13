package com.kuss.krude.utils

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.kuss.krude.db.AppInfo
import timber.log.Timber

object AppHelper {
    @JvmStatic
    fun getInstalled(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val allApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            val flags =
                PackageManager.MATCH_UNINSTALLED_PACKAGES or PackageManager.MATCH_ARCHIVED_PACKAGES.toInt()
            pm.getInstalledApplications(flags or 0)
        } else {
            pm.getInstalledApplications(0)
        }
        Timber.d("getInstalledApplications: ${allApps.joinToString { it.packageName }}")
        val validApps: MutableList<AppInfo> = ArrayList()
        for (app in allApps) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
                if (!app.isArchived && pm.getLaunchIntentForPackage(app.packageName) == null) continue
            } else {
                if (pm.getLaunchIntentForPackage(app.packageName) == null) {
                    continue
                }
            }
            validApps.add(getAppInfo(app, pm, context))
        }
        return FilterHelper.getSorted(validApps)
    }

    @JvmStatic
    fun getAllPackageNames(context: Context): List<String> {
        val pm = context.packageManager
        val allApps = pm.getInstalledApplications(PackageManager.MATCH_ALL)
        return allApps.map {
            it.packageName
        }
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