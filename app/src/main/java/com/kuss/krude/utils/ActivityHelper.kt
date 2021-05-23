package com.kuss.krude.utils

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.kuss.krude.AppDetailActivity
import com.kuss.krude.data.AppInfo


object ActivityHelper {
    @JvmStatic
    fun startWithRevealAnimation(context: Context, view: View, packageName: String) {
        val intent = context
            .packageManager.getLaunchIntentForPackage(packageName)
            ?.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ?: return

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
    fun startAppDetail(context: Context, view: View, item: AppInfo) {
        val nextIntent = Intent(context, AppDetailActivity::class.java).apply {
            putExtra("label", item.label)
            putExtra("packageName", item.packageName)
        }

        startWithRevealAnimation(
            context,
            view,
            nextIntent,
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
    fun checkOrSetDefaultLauncher(activity: Activity) {
        if (!isDefaultLauncher(activity)) {
            AlertDialog.Builder(activity).apply {
                setTitle("Set Krude as default launcher?")
                setPositiveButton("Go to set") { _: DialogInterface, _: Int ->
                    startDefaultHome(activity)
                }
                setNegativeButton("No") { dialogInterface: DialogInterface, _: Int ->
                    dialogInterface.cancel()
                }
            }.show()
        }
    }

    @JvmStatic
    fun isDefaultLauncher(activity: Activity): Boolean {
        val intent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
        }
        val resolveInfo =
            activity.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val currentHomePackage = resolveInfo!!.activityInfo.packageName
        return currentHomePackage.equals(activity.packageName)
    }
}