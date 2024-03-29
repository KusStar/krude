package com.kuss.krude.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat

object ActivityHelper {
    @JvmStatic
    fun startWithRevealAnimation(context: Context, view: View, intent: Intent) {
        val compat = ActivityOptionsCompat.makeClipRevealAnimation(
            view, 0, 0, view.width, view.height
        )
        ActivityCompat.startActivity(
            context,
            intent,
            compat.toBundle()
        )
    }

    @JvmStatic
    fun findActivitiesForPackage(context: Context, packageName: String): List<ResolveInfo?>? {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
            setPackage(packageName)
        }
        return packageManager.queryIntentActivities(mainIntent, 0).distinctBy {
            it.activityInfo.applicationInfo.packageName
        }
    }

    @JvmStatic
    fun startDefaultHome(activity: Activity) {
        startWithRevealAnimation(
            activity,
            activity.window.decorView,
            Intent(Settings.ACTION_HOME_SETTINGS)
        )
    }

    @JvmStatic
    fun toDetail(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:$packageName")
        }
        ActivityCompat.startActivity(
            context,
            intent,
            null
        )
    }

    @JvmStatic
    fun toUninstall(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
        }
        ActivityCompat.startActivity(
            context,
            intent,
            null
        )
    }
}