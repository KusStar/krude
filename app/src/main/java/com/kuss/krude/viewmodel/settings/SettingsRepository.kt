package com.kuss.krude.viewmodel.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kuss.krude.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object DominantHandDefaults {
    const val LEFT = "left"
    const val RIGHT = "right"
}

object ExtensionDisplayModeDefaults {
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
    val extensionGroupLayout: Boolean = true,
    val customKeyboardScale: Float = 1f,
    val customKeyboardOffset: Int = 0,
    val devMode: Boolean = false,
    val devExtension: Boolean = false,
    val devExtensionRepo: String = "http://localhost:12345",
)

val DEFAULT_SETTINGS_STATE = SettingsState()

class SettingsRepository(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = if (BuildConfig.DEBUG) "settings_debug" else "settings" )
        val AUTO_FOCUS_KEY = booleanPreferencesKey("auto_focus")
        val FUZZY_SEARCH_KEY = booleanPreferencesKey("fuzzy_search")
        val SHOW_USAGE_COUNT_KEY = booleanPreferencesKey("show_usage_count")
        val USE_EMBED_KEYBOARD_KEY = booleanPreferencesKey("use_embed_keyboard")
        val SHOW_SEARCH_HISTORY_KEY = booleanPreferencesKey("show_search_history")
        val SHOW_LEFT_SIDE_BACKSPACE_KEY = booleanPreferencesKey("show_left_side_backspace")
        val DOMINANT_HAND_KEY = stringPreferencesKey("dominant_hand")
        val ENABLE_EXTENSION_KEY = booleanPreferencesKey("enable_extension")
        val EXTENSION_DISPLAY_MODE_KEY = stringPreferencesKey("extension_display_mode")
        val EXTENSION_GROUP_LAYOUT_KEY = booleanPreferencesKey("extension_group_layout")
        val CUSTOM_KEYBOARD_SCALE_KEY = floatPreferencesKey("custom_keyboard_scale")
        val CUSTOM_KEYBOARD_OFFSET_KEY = intPreferencesKey("custom_keyboard_offset")
        val DEV_MODE_KEY = booleanPreferencesKey("dev_mode")
        val DEV_EXTENSION_KEY = booleanPreferencesKey("dev_extension")
        val DEV_EXTENSION_REPO_KEY = stringPreferencesKey("dev_extension_repo")
    }

    suspend fun saveBoolSetting(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    suspend fun saveStringSetting(key: Preferences.Key<String>, value: String) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    suspend fun saveFloatSetting(key: Preferences.Key<Float>, value: Float) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    suspend fun saveIntSetting(key: Preferences.Key<Int>, value: Int) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    // Get all settings as a flow
    val settingsFlow: Flow<SettingsState> = context.dataStore.data
        .map { preferences ->
            SettingsState(
                autoFocus = preferences[AUTO_FOCUS_KEY] ?: DEFAULT_SETTINGS_STATE.autoFocus,
                fuzzySearch = preferences[FUZZY_SEARCH_KEY] ?: DEFAULT_SETTINGS_STATE.fuzzySearch,
                showUsageCount = preferences[SHOW_USAGE_COUNT_KEY] ?: DEFAULT_SETTINGS_STATE.showUsageCount,
                useEmbedKeyboard = preferences[USE_EMBED_KEYBOARD_KEY] ?: DEFAULT_SETTINGS_STATE.useEmbedKeyboard,
                showSearchHistory = preferences[SHOW_SEARCH_HISTORY_KEY] ?: DEFAULT_SETTINGS_STATE.showSearchHistory,
                showLeftSideBackSpace = preferences[SHOW_LEFT_SIDE_BACKSPACE_KEY] ?: DEFAULT_SETTINGS_STATE.showLeftSideBackSpace,
                dominantHand = preferences[DOMINANT_HAND_KEY] ?: DEFAULT_SETTINGS_STATE.dominantHand,
                enableExtension = preferences[ENABLE_EXTENSION_KEY] ?: DEFAULT_SETTINGS_STATE.enableExtension,
                extensionDisplayMode = preferences[EXTENSION_DISPLAY_MODE_KEY] ?: DEFAULT_SETTINGS_STATE.extensionDisplayMode,
                extensionGroupLayout = preferences[EXTENSION_GROUP_LAYOUT_KEY] ?: DEFAULT_SETTINGS_STATE.extensionGroupLayout,
                customKeyboardScale = preferences[CUSTOM_KEYBOARD_SCALE_KEY] ?: DEFAULT_SETTINGS_STATE.customKeyboardScale,
                customKeyboardOffset = preferences[CUSTOM_KEYBOARD_OFFSET_KEY] ?: DEFAULT_SETTINGS_STATE.customKeyboardOffset,
                // dev mode
                devMode = preferences[DEV_MODE_KEY] ?: DEFAULT_SETTINGS_STATE.devMode,
                devExtension = preferences[DEV_EXTENSION_KEY] ?: DEFAULT_SETTINGS_STATE.devExtension,
                devExtensionRepo =  preferences[DEV_EXTENSION_REPO_KEY] ?: DEFAULT_SETTINGS_STATE.devExtensionRepo,
            )
        }
}