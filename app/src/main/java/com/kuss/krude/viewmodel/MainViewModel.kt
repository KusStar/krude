package com.kuss.krude.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuss.krude.data.AppInfoWithIcon
import com.kuss.krude.utils.AppHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainState(
    val apps: List<AppInfoWithIcon> = listOf(),
    val filteredApps: List<AppInfoWithIcon> = listOf(),
    val scrollbarItems: List<String> = listOf(),
    val currentScrollbarIndex: Int = 0,
    val filtering: String = "",
    val showAppDetailSheet: Boolean = false,
    val selectedDetailApp: AppInfoWithIcon? = null
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainState())

    val state: StateFlow<MainState>
        get() = _state

    fun loadApps(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                _state.update { mainState ->
                    val items = AppHelper.getInstalled(context)
                    mainState.copy(
                        apps = items,
                        scrollbarItems = getScrollbarItemsFromApps(items)
                    )
                }
            }
        }
    }

    fun setFiltering(filtering: String) {
        _state.update { mainState ->
            mainState.copy(filtering = filtering)
        }
    }

    fun setFilteredApps(apps: List<AppInfoWithIcon>) {
        _state.update { mainState ->
            mainState.copy(filteredApps = apps)
        }
    }

    fun setShowAppDetailSheet(visible: Boolean) {
        _state.update { mainState ->
            mainState.copy(
                showAppDetailSheet = visible
            )
        }
    }

    fun setSelectedDetailApp(app: AppInfoWithIcon) {
        _state.update { mainState ->
            mainState.copy(
                selectedDetailApp = app
            )
        }
    }

    fun setSelectedHeaderIndex(i: Int) {
        _state.update { mainState ->
            mainState.copy(
                currentScrollbarIndex = i
            )
        }
    }

    private fun getScrollbarItemsFromApps(apps: List<AppInfoWithIcon>): List<String> {
        return apps.map { it.abbr.first().uppercase() }
            .toSet().toList().sorted()
    }
}