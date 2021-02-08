package com.kuss.krude.utils

import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import com.kuss.krude.AppDetailActivity
import com.kuss.krude.data.AppInfo


object ActivityHelper  {
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
        val nextIntent = Intent(context, AppDetailActivity::class.java)
        nextIntent.putExtra("label", item.label)
        nextIntent.putExtra("packageName", item.packageName)

        startWithRevealAnimation(
            context,
            view,
            nextIntent,
        )
    }

    @JvmStatic
    fun findActivitiesForPackage(context: Context, packageName: String): List<ResolveInfo?>? {
        val packageManager = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)
        mainIntent.setPackage(packageName)
        val apps = packageManager.queryIntentActivities(mainIntent, 0)
        return apps ?: ArrayList<ResolveInfo>()
    }
}