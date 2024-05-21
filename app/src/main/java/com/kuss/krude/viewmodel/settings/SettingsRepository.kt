package com.kuss.krude.viewmodel.settings

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.kuss.krude.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {

    companion object {
        private val Context.dataStore by preferencesDataStore(name = if (BuildConfig.DEBUG) "settings_debug" else "settings" )
        val AUTO_FOCUS_KEY = booleanPreferencesKey("auto_focus")
        val FUZZY_SEARCH_KEY = booleanPreferencesKey("fuzzy_search")
        val SHOW_USAGE_COUNT_KEY = booleanPreferencesKey("show_usage_count")
        val USE_EMBED_KEYBOARD_KEY = booleanPreferencesKey("use_embed_keyboard")
        val SHOW_SEARCH_HISTORY_KEY = booleanPreferencesKey("show_search_history")
        val SHOW_LEFT_SIDE_BACKSPACE = booleanPreferencesKey("show_left_side_backspace")
        val DOMINANT_HAND = stringPreferencesKey("dominant_hand")
        val ENABLE_EXTENSION = booleanPreferencesKey("enable_extension")
        val EXTENSION_DISPLAY_MODE = stringPreferencesKey("extension_display_mode")
        val DEV_MODE = booleanPreferencesKey("dev_mode")
        val DEV_EXTENSION = booleanPreferencesKey("dev_extension")
        val DEV_EXTENSION_REPO = stringPreferencesKey("dev_extension_repo")
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

    // Get all settings as a flow
    val settingsFlow: Flow<SettingsState> = context.dataStore.data
        .map { preferences ->
            SettingsState(
                autoFocus = preferences[AUTO_FOCUS_KEY] ?: DEFAULT_SETTINGS_STATE.autoFocus,
                fuzzySearch = preferences[FUZZY_SEARCH_KEY] ?: DEFAULT_SETTINGS_STATE.fuzzySearch,
                showUsageCount = preferences[SHOW_USAGE_COUNT_KEY] ?: DEFAULT_SETTINGS_STATE.showUsageCount,
                useEmbedKeyboard = preferences[USE_EMBED_KEYBOARD_KEY] ?: DEFAULT_SETTINGS_STATE.useEmbedKeyboard,
                showSearchHistory = preferences[SHOW_SEARCH_HISTORY_KEY] ?: DEFAULT_SETTINGS_STATE.showSearchHistory,
                showLeftSideBackSpace = preferences[SHOW_LEFT_SIDE_BACKSPACE] ?: DEFAULT_SETTINGS_STATE.showLeftSideBackSpace,
                dominantHand = preferences[DOMINANT_HAND] ?: DEFAULT_SETTINGS_STATE.dominantHand,
                enableExtension = preferences[ENABLE_EXTENSION] ?: DEFAULT_SETTINGS_STATE.enableExtension,
                extensionDisplayMode = preferences[EXTENSION_DISPLAY_MODE] ?: DEFAULT_SETTINGS_STATE.extensionDisplayMode,
                // dev mode
                devMode = preferences[DEV_MODE] ?: DEFAULT_SETTINGS_STATE.devMode,
                devExtension = preferences[DEV_EXTENSION] ?: DEFAULT_SETTINGS_STATE.devExtension,
                devExtensionRepo =  preferences[DEV_EXTENSION_REPO] ?: DEFAULT_SETTINGS_STATE.devExtensionRepo,
            )
        }
}