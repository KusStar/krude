package com.kuss.krude.utils

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent

object DeeplinkHelper {
    @SuppressLint("WrongConstant")
    @JvmStatic
    fun openWechatScan(context: Context): Boolean {

        try {
            val intent = Intent()
            intent.setComponent(ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI"))
            intent.putExtra("LauncherUI.From.Scaner.Shortcut", true)
            intent.setFlags(335544320)
            intent.setAction("android.intent.action.VIEW")
            context.startActivity(intent)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}