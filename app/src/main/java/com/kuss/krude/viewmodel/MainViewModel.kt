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
    val items: List<AppInfoWithIcon> = listOf(),
    val filteredItems: List<AppInfoWithIcon> = listOf(),
    val headers: List<String> = listOf(),
    val selectedHeaderIndex: Int = 0,
    val filtering: String = "",
    val showAppDetailSheet: Boolean = false,
    val selectedDetailApp: AppInfoWithIcon? = null
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(MainState())

    val state: StateFlow<MainState>
        get() = _state

    fun loadItems(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                _state.update { mainState ->
                    val items = AppHelper.getInstalled(context)
                    mainState.copy(
                        items = items,
                        headers = getHeadersFromItems(items)
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

    fun setFilteredItems(filteredItems: List<AppInfoWithIcon>) {
        _state.update { mainState ->
            mainState.copy(filteredItems = filteredItems)
        }
    }

    fun updateHeaders() {
        _state.update { mainState ->
            mainState.copy(
                headers = getHeadersFromItems(mainState.items)
            )
        }
    }

    fun setShowAppDetailSheet(visible: Boolean) {
        _state.update { mainState ->
            mainState.copy(
                showAppDetailSheet = visible
            )
        }
    }

    fun setSelectedDetailApp(item: AppInfoWithIcon) {
        _state.update { mainState ->
            mainState.copy(
                selectedDetailApp = item
            )
        }
    }

    fun setSelectedHeaderIndex(i: Int) {
        _state.update { mainState ->
            mainState.copy(
                selectedHeaderIndex = i
            )
        }
    }

    private fun getHeadersFromItems(items: List<AppInfoWithIcon>): List<String> {
        return items.map { it.abbr.first().uppercase() }
            .toSet().toList().sorted()
    }
}