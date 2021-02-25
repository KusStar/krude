package com.kuss.krude

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.kuss.krude.ui.AppListFragment
import com.kuss.krude.ui.FilteredListFragment
import com.kuss.krude.ui.MainFragment
import com.kuss.krude.utils.PinyinHelper


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .replace(R.id.list_container, AppListFragment.newInstance())
                .replace(R.id.filtered_list_container, FilteredListFragment.newInstance())
                .commitNow()
        }

        PinyinHelper.initDict()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        }
    }

    override fun onBackPressed() {}
}