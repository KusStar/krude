package com.kuss.krude.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuss.krude.R
import com.kuss.krude.adapters.AppListAdapter
import com.kuss.krude.data.AppInfo
import com.kuss.krude.models.AppViewModel
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.KeyboardHelper
import java.text.Collator
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.sortWith


class AppListFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.allApps.value = getApps()

        val view = inflater.inflate(R.layout.app_item_list, container, false)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = GridLayoutManager(context, 2)
                model.allApps.observe(viewLifecycleOwner, { apps ->
                    adapter = AppListAdapter(apps,
                        object : AppListAdapter.OnItemClickListener {
                            override fun onClick(view: View, packageName: String) {
                                KeyboardHelper.hide(requireActivity())
                                launchApp(view, packageName)
                            }

                            override fun onLongClick(item: AppInfo) {
                                ActivityHelper.startAppDetail(
                                    requireContext(),
                                    requireView(),
                                    item
                                )
                            }
                        })
                })

            }
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        initBroadcastReceiver()
    }

    private fun getApps(): List<AppInfo> {
        val pm = requireContext().packageManager
        val allApps = pm.getInstalledApplications(0)
        val validApps: MutableList<AppInfo> = ArrayList()
        for (app in allApps) {
            if (pm.getLaunchIntentForPackage(app.packageName) == null) continue

            val label = app.loadLabel(pm).toString()
            val packageName = app.packageName
            val icon = app.loadIcon(pm)
            val filterTarget = FilterHelper.toTarget(label, packageName)
            validApps.add(AppInfo(label, packageName, icon, filterTarget))
        }
        validApps.sortWith { s1, s2 ->
            Collator.getInstance(Locale.CHINESE).compare(s1.label, s2.label)
        }
        return validApps
    }

    private fun launchApp(view: View, packageName: String) {
        ActivityHelper.startWithRevealAnimation(
            requireContext(),
            view,
            packageName
        )
    }

    private fun initBroadcastReceiver() {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                model.allApps.value = getApps()
                model.clearApps()
                model.clearSearch()
            }
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_INSTALL)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED)
        intentFilter.addDataScheme("package");
        requireContext().registerReceiver(receiver, intentFilter)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppListFragment()
    }

}