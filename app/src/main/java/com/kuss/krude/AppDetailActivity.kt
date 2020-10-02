package com.kuss.krude

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.kuss.krude.utils.ActivityHelper
import kotlinx.android.synthetic.main.activity_app_detail.*


class AppDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        intent?.let {
            val label = it.getStringExtra("label")
            val packageName = it.getStringExtra("packageName")

            val icon = packageManager.getApplicationIcon(packageName!!)
            icon_view.setImageDrawable(icon)

            label_view.text = label
            package_name_view.text = packageName

            detail_btn.setOnClickListener {
                toDetail(packageName)
            }

            uninstall_btn.setOnClickListener {
                toUninstall(packageName)
            }
        }
    }

    private fun toDetail(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.data = Uri.parse("package:$packageName")

        ActivityHelper.startWithRevealAnimation(
            this,
            this.icon_view,
            intent
        )
        finish()
    }

    private fun toUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
        intent.data = Uri.parse("package:$packageName")
        ActivityHelper.startWithRevealAnimation(
            this,
            this.icon_view,
            intent
        )
        finish()
    }
}

