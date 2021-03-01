package com.kuss.krude

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.kuss.krude.utils.ActivityHelper
import kotlinx.android.synthetic.main.activity_app_detail.*
import me.zhanghai.android.appiconloader.AppIconLoader


class AppDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_detail)

        intent?.let {
            val label = it.getStringExtra("label")
            val packageName = it.getStringExtra("packageName")

            val info = packageManager.getPackageInfo(packageName!!, 0)
            val iconSize = resources.getDimensionPixelSize(R.dimen.detail_icon_size)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                version_info.text = "${info.versionName}(${info.longVersionCode})"
            } else {
                version_info.text = "${info.versionName}(${info.versionCode})"
            }

            icon_view.setImageBitmap(
                AppIconLoader(iconSize, true, this)
                    .loadIcon(info.applicationInfo)
            )

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
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            data = Uri.parse("package:$packageName")
        }
        ActivityHelper.startWithRevealAnimation(
            this,
            this.icon_view,
            intent
        )
        finish()
    }

    private fun toUninstall(packageName: String) {
        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
            data = Uri.parse("package:$packageName")
        }
        ActivityHelper.startWithRevealAnimation(
            this,
            this.icon_view,
            intent
        )
        finish()
    }
}

