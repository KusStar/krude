package com.kuss.krude.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kuss.krude.R
import com.kuss.krude.adapters.AppListAdapter
import com.kuss.krude.models.AppInfo
import com.kuss.krude.models.AppViewModel
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.KeyboardHelper

class FilteredListFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.filtered_item_list, container, false)

        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(
                    context,
                    LinearLayoutManager.HORIZONTAL, false
                )
                model.apps.observe(viewLifecycleOwner, { apps ->
                    adapter = AppListAdapter(apps,
                        object : AppListAdapter.OnItemClickListener {
                            override fun onClick(view: View, packageName: String) {
                                KeyboardHelper.hide(requireActivity())
                                launchApp(packageName)
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

    private fun launchApp(packageName: String) {
        ActivityHelper.startWithRevealAnimation(
            requireContext(),
            requireView(),
            packageName
        )
        model.clearSearch()
    }

    companion object {
        @JvmStatic
        fun newInstance() = FilteredListFragment()
    }
}