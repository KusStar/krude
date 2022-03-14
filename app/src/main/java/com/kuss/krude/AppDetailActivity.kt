package com.kuss.krude

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.kuss.krude.databinding.ActivityAppDetailBinding
import com.kuss.krude.utils.ActivityHelper
import me.zhanghai.android.appiconloader.AppIconLoader


class AppDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAppDetailBinding

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        intent?.let {
            val label = it.getStringExtra("label")
            val packageName = it.getStringExtra("packageName")

            val info = packageManager.getPackageInfo(packageName!!, 0)
            val iconSize = resources.getDimensionPixelSize(R.dimen.detail_icon_size)

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                binding.versionInfo.text = "${info.versionName}(${info.longVersionCode})"
            } else {
                binding.versionInfo.text = "${info.versionName}(${info.versionCode})"
            }

            binding.iconView.setImageBitmap(
                AppIconLoader(iconSize, true, this)
                    .loadIcon(info.applicationInfo)
            )

            binding.labelView.text = label
            binding.packageNameView.text = packageName

            binding.detailBtn.setOnClickListener {
                toDetail(packageName)
            }

            binding.uninstallBtn.setOnClickListener {
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
            binding.iconView,
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
            binding.iconView,
            intent
        )
        finish()
    }
}

