package com.kuss.krude.viewmodel.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object DominantHandDefaults {
    const val LEFT = "left"
    const val RIGHT = "right"
}

object ExtensionDisplayModeDefaults {
    const val IN_LINE = "in_line"
    const val ON_TOP = "on_top"
    const val ON_BOTTOM = "on_bottom"
}

data class SettingsState(
    val autoFocus: Boolean = true,
    val fuzzySearch: Boolean = true,
    val showUsageCount: Boolean = false,
    val enableExtension: Boolean = true,
    val useEmbedKeyboard: Boolean = true,
    val showSearchHistory: Boolean = true,
    val showLeftSideBackSpace: Boolean = true,
    val dominantHand: String = DominantHandDefaults.LEFT,
    val extensionDisplayMode: String = ExtensionDisplayModeDefaults.ON_TOP,
    val devMode: Boolean = false,
    val devExtension: Boolean = false,
    val devExtensionRepo: String = "http://localhost:12345",
)

val DEFAULT_SETTINGS_STATE = SettingsState()

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())

    val state: StateFlow<SettingsState> = _state

    init {
        // Load settings initially
        loadSettings()
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[APPLICATION_KEY])
                val settingsRepository = SettingsRepository(application.applicationContext)

                return SettingsViewModel(
                    settingsRepository
                ) as T
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { settings ->
                _state.value = settings
            }
        }
    }

    // Functions to update each setting, calling the repository to save the new value
    fun setAutoFocus(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.AUTO_FOCUS_KEY, enabled)
            loadSettings() // Reload settings to update UI
        }
    }

    fun setFuzzySearch(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.FUZZY_SEARCH_KEY, enabled)
            loadSettings()
        }
    }

    fun setShowUsageCount(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.SHOW_USAGE_COUNT_KEY, enabled)
            loadSettings()
        }
    }

    fun setUseEmbedKeyboard(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.USE_EMBED_KEYBOARD_KEY, enabled)
            loadSettings()
        }
    }

    fun setShowSearchHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.SHOW_SEARCH_HISTORY_KEY, enabled)
            loadSettings()
        }
    }

    fun setShowLeftSideBackspace(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.SHOW_LEFT_SIDE_BACKSPACE, enabled)
            loadSettings()
        }
    }

    fun setDominantHand(hand: String) {
        viewModelScope.launch {
            settingsRepository.saveStringSetting(SettingsRepository.DOMINANT_HAND, hand)
            loadSettings()
        }
    }

    fun setEnableExtension(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.ENABLE_EXTENSION, enabled)
            loadSettings()
        }
    }

    fun setExtensionDisplayMode(mode: String) {
        viewModelScope.launch {
            settingsRepository.saveStringSetting(SettingsRepository.EXTENSION_DISPLAY_MODE, mode)
            loadSettings()
        }
    }

    fun setDevMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.DEV_MODE, enabled)
            loadSettings()
        }
    }

    fun setDevExtension(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.saveBoolSetting(SettingsRepository.DEV_EXTENSION, enabled)
            loadSettings()
        }
    }

    fun setDevExtensionRepo(url: String) {
        viewModelScope.launch {
            settingsRepository.saveStringSetting(SettingsRepository.DEV_EXTENSION_REPO, url)
            loadSettings()
        }
    }
}