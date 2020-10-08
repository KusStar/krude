package com.kuss.krude.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kuss.krude.R
import com.kuss.krude.databinding.MainFragmentBinding
import com.kuss.krude.models.AppViewModel
import com.kuss.krude.utils.FilterHelper
import kotlinx.android.synthetic.main.main_fragment.*


class MainFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = DataBindingUtil.inflate<MainFragmentBinding>(
            inflater, R.layout.main_fragment, container, false
        )
        binding.viewModel = model

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dealSoftInput()

        editText.requestFocus()

        model.search.observe(viewLifecycleOwner, { search ->
            // TODO: fix duplicating excuting when search.isEmpty()
            if (search.isEmpty()) {
                editText.text.clear()
                model.clearApps()
            } else {
                filterApps(search)
            }
        })

    }

    private fun dealSoftInput() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(true)
        } else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun filterApps(search: String) {
        model.allApps.value?.let { apps ->
            model.apps.value = FilterHelper.getFiltered(search, apps)
        }
    }

}