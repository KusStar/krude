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
import com.kuss.krude.utils.*


class AppListFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()
    private val receiver = Receiver()
    private var spanCount = 2
    private lateinit var recycler: RecyclerView
    private lateinit var manager: GridLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        model.allApps.value = AppHelper.getInstalled(requireContext())

        recycler = inflater.inflate(R.layout.app_item_list, container, false) as RecyclerView

        manager = GridLayoutManager(context, spanCount)

        recycler.layoutManager = manager

        recycler.adapter = AppListAdapter(
            listOf(),
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

        model.allApps.observe(viewLifecycleOwner, { apps ->
            (recycler.adapter as AppListAdapter).apps = apps
        })

        recycler.addOnItemTouchListener(ScaleGestureItemTouchListener(context, object :
            ScaleGestureItemTouchListener.Callback {
            override fun onScaleFactor(scaleFactor: Float) {
                val next = MAX_SPAN - scaleFactor.toInt()
                if (next < 1 || next == spanCount) return
                spanCount = next
                manager.spanCount = spanCount
                updateRecycler()
            }
        }))

        return recycler
    }

    private fun updateRecycler() {
        val adapter = recycler.adapter as AppListAdapter

        val manager = recycler.layoutManager as GridLayoutManager
        val firstVisible = manager.findFirstVisibleItemPosition()
        val lastVisible = manager.findLastCompletelyVisibleItemPosition()

        adapter.showLabel = spanCount < 4
        adapter.notifyItemRangeChanged(firstVisible + 1, lastVisible - firstVisible - 1)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                }
            }
        }
    }

    fun handlePackageAdded(intent: Intent) {
        val ctx = context ?: return

        val intentPackageName = intent.dataString?.substring(8)
            ?: return
        val list = ActivityHelper
            .findActivitiesForPackage(ctx, intentPackageName)
            ?: return
        val apps = model.allApps.value?.toMutableList()
            ?: return

        val pm = ctx.packageManager ?: return

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

        FilterHelper.getSorted(apps).let { sorted ->
            recycler.adapter?.notifyItemInserted(
                sorted.indexOfFirst { it.packageName == intentPackageName }
            )
            model.allApps.value = sorted
        }

    }

    fun handlePackageRemoved(intent: Intent) {
        val apps = model.allApps.value?.toMutableList()
            ?: return
        val toDeletePackageName = intent.dataString?.substring(8)
            ?: return

        val removedIndex = apps.indexOfFirst { it.packageName == toDeletePackageName }
        if (removedIndex != -1) {
            recycler.adapter?.notifyItemRemoved(removedIndex)
            apps.removeAt(removedIndex)
            model.allApps.value = apps
        }

    }

    fun postHandle() {
        model.clearApps()
        model.clearSearch()
    }

    companion object {
        @JvmStatic
        fun newInstance() = AppListFragment()
        const val MAX_SPAN = 4
    }

}