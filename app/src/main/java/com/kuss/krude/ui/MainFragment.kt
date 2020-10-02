package com.kuss.krude.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.kuss.krude.R
import com.kuss.krude.models.AppViewModel
import kotlinx.android.synthetic.main.main_fragment.editText
import java.util.*

class MainFragment : Fragment() {
    private val model: AppViewModel by activityViewModels()

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        dealSoftInput()

        initEditText()

        model.apps.observe(viewLifecycleOwner, {apps ->
            if (apps.isEmpty()) {
                editText.text.clear()
            }
        })
    }

    private fun initEditText() {
        editText.requestFocus()

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterApps(s)
            }

            override fun afterTextChanged(s: Editable?) { }
        })
    }

    private fun dealSoftInput() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            requireActivity().window.setDecorFitsSystemWindows(true)
        } else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        }
    }

    private fun filterApps(s: CharSequence?) {
        model.getAllApps().let {apps ->
            model.apps.value =
                if (s.isNullOrEmpty()) {
                    emptyList()
                } else {
                    apps.filter { app ->
                        app.filterTarget.toLowerCase(Locale.ROOT)
                            .contains(s.toString().toLowerCase(Locale.ROOT))
                    }
                }
        }
    }

}