package com.kuss.krude.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import java.lang.ref.WeakReference

object ActivityHelper {
    private var activity: WeakReference<Activity>? = null

    @JvmStatic
    fun initActivity(activity: Activity) {
        this.activity = WeakReference(activity)
    }

    @JvmStatic
    fun startPackageActivity(context: Context, packageName: String, view: View? = null) {
        val intent = context
            .packageManager.getLaunchIntentForPackage(packageName)
            ?: return
        startIntentWithTransition(context, intent, view)
    }

    @JvmStatic
    fun startIntentWithTransition(context: Context, intent: Intent, argView: View? = null) {
        val view = argView ?: activity?.get()?.window?.decorView
        var bundle: Bundle? = null
        if (view != null) {
            val w = view.measuredWidth
            val h = view.measuredHeight
            val startWidth = w / 4
            val startHeight = h / 4
            val activityOptionsCompat: ActivityOptionsCompat =
                ActivityOptionsCompat.makeScaleUpAnimation(
                    view,
                    w / 2 - startWidth / 2, h + startHeight, startWidth, startHeight
                )
            bundle = activityOptionsCompat.toBundle()
        }
        if (bundle == null) {
            bundle = Bundle()
        }
        // show Android 12 splash screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bundle.putInt("android.activity.splashScreenStyle", 1)
        }

        ActivityCompat.startActivity(
            context,
            intent,
            bundle
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
    fun toDetail(context: Context, packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:$packageName")
        }
        startIntentWithTransition(context, intent)
    }

    @JvmStatic
    fun toUninstall(context: Context, packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
        }
        startIntentWithTransition(context, intent)
    }

    @SuppressLint("WrongConstant")
    @JvmStatic
    fun openWechatScan(context: Context): Boolean {
        try {
            val intent = Intent()
            intent.setComponent(ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"))
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
            intent.setFlags(335544320)
            intent.setAction("android.intent.action.VIEW")
            startIntentWithTransition(context, intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}