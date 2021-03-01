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
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.KeyboardHelper


class AppListFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()
    private val receiver = Receiver()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        model.allApps.value = AppHelper.getInstalled(requireContext())

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

    private fun launchApp(view: View, packageName: String) {
        ActivityHelper.startWithRevealAnimation(
            requireContext(),
            view,
            packageName
        )
    }

    private fun initBroadcastReceiver() {
        val intentFilter = IntentFilter()
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)
        intentFilter.addDataScheme("package")
        requireContext().registerReceiver(receiver, intentFilter)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppListFragment()
    }

    inner class Receiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            intent.action?.let { action ->
                when (action) {
                    Intent.ACTION_PACKAGE_ADDED -> {
                        handlePackageAdded(intent)
                        postHandle()
                    }
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        handlePackageRemoved(intent)
                        postHandle()
                    }
                    else -> return
                }
            }
        }
    }

    fun handlePackageAdded(intent: Intent) {
        val intentPackageName = intent.dataString?.substring(8)
            ?: return
        val list = ActivityHelper
            .findActivitiesForPackage(requireContext(), intentPackageName)
            ?: return
        val apps = model.allApps.value?.toMutableList()
            ?: return

        val pm = requireContext().packageManager
        for (item in list) {
            if (item == null) continue
            try {
                apps.add(
                    AppHelper.getAppInfo(
                        item.activityInfo.applicationInfo,
                        pm,
                        requireContext()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
        }
        model.allApps.value = FilterHelper.getSorted(apps)
    }

    fun handlePackageRemoved(intent: Intent) {
        val apps = model.allApps.value?.toMutableList()
            ?: return
        val toDeletePackageName = intent.dataString?.substring(8)
            ?: return

        model.allApps.value = apps.filterNot { it -> it.packageName == toDeletePackageName }
    }

    fun postHandle() {
        model.clearApps()
        model.clearSearch()
    }


}