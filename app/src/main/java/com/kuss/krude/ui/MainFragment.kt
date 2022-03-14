package com.kuss.krude.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kuss.krude.databinding.MainFragmentBinding
import com.kuss.krude.models.AppViewModel
import com.kuss.krude.utils.FilterHelper


class MainFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()
    private var _binding: MainFragmentBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MainFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = model

        init()

        return binding.root
    }

    private fun init() {
        dealSoftInput()

        binding.filterText.requestFocus()

        model.search.observe(viewLifecycleOwner) { search ->
            // TODO: fix duplicating executing when search.isEmpty()
            if (search.isEmpty()) {
                binding.filterText.text.clear()
                model.clearApps()
            } else {
                filterApps(search)
            }
        }
    }

    private fun dealSoftInput() {
        requireActivity().window.apply {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(true)
            } else {
                setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            }
        }
    }

    private fun filterApps(search: String) {
        model.allApps.value?.let { apps ->
            model.apps.value = FilterHelper.getFiltered(search, apps)
                .sortedByDescending { it.priority }
        }
    }

}