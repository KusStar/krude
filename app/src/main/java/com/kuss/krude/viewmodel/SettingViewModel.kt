package com.kuss.krude.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class SettingState(
    val autoFocus: Boolean = false,
    val fuzzySearch: Boolean = false,
    val showUsageCount: Boolean = false,
    val embedKeyboard: Boolean = false,
    val showSearchHistory: Boolean = true
)

class SettingViewModel: ViewModel() {
    private val _state = MutableStateFlow(SettingState())

    val state: StateFlow<SettingState>
        get() = _state

    fun setAutoFocus(value: Boolean) {
        _state.update {
            it.copy(autoFocus = value)
        }
    }

    fun setFuzzySearch(value: Boolean) {
        _state.update {
            it.copy(fuzzySearch = value)
        }
    }

    fun setShowUsageCount(value: Boolean) {
        _state.update {
            it.copy(showUsageCount = value)
        }
    }

    fun setEmbedKeyboard(value: Boolean) {
        _state.update {
            it.copy(embedKeyboard = value)
        }
    }

    fun setShowSearchHistory(value: Boolean) {
        _state.update {
            it.copy(showSearchHistory = value)
        }
    }

}