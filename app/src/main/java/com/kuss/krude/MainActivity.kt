package com.kuss.krude

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kuss.krude.databinding.MainActivityBinding
import com.kuss.krude.ui.AppListFragment
import com.kuss.krude.ui.FilteredListFragment
import com.kuss.krude.ui.MainFragment
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.PinyinHelper


class MainActivity : AppCompatActivity() {
    private lateinit var binding: MainActivityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        PinyinHelper.initDict()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }

        ActivityHelper.checkOrSetDefaultLauncher(this) {
            commitFragments()
        }
    }

    private fun commitFragments() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, MainFragment.newInstance())
            .replace(R.id.list_container, AppListFragment.newInstance())
            .replace(R.id.filtered_list_container, FilteredListFragment.newInstance())
            .commitNow()
    }

    override fun onBackPressed() {
        if (!ActivityHelper.isDefaultLauncher(this)) {
            super.onBackPressed()
        }
    }
}