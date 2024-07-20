package com.kuss.krude

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.kuss.krude.utils.ActivityHelper


class BrowserActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.action == Intent.ACTION_VIEW) {
            val uri = intent.data
            val intent = Intent(Intent.ACTION_VIEW, uri)
            val pm: PackageManager = this.packageManager
            val mInfo = pm.resolveActivity(intent, 0)
            if (mInfo != null) {
                val target = pm.getLaunchIntentForPackage(mInfo.activityInfo.packageName)
                    ?: return
                target.data = uri
                ActivityHelper.startIntentWithTransition(
                    this,
                    target,
                    isFreeformWindow = true
                )
            }
        }
        finish()
    }
}
