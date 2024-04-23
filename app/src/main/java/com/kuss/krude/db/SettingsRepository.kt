package com.kuss.krude.db

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.kuss.krude.viewmodel.DEFAULT_SETTINGS_STATE
import com.kuss.krude.viewmodel.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    // Create the DataStore
    private val Context.dataStore by preferencesDataStore(name = "settings")

    // Define keys for your settings
    companion object {
        val AUTO_FOCUS_KEY = booleanPreferencesKey("auto_focus")
        val FUZZY_SEARCH_KEY = booleanPreferencesKey("fuzzy_search")
        val SHOW_USAGE_COUNT_KEY = booleanPreferencesKey("show_usage_count")
        val USE_EMBED_KEYBOARD_KEY = booleanPreferencesKey("use_embed_keyboard")
        val SHOW_SEARCH_HISTORY_KEY = booleanPreferencesKey("show_search_history")
    }

    // Save a setting
    suspend fun saveSetting(key: Preferences.Key<Boolean>, value: Boolean) {
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
                showSearchHistory = preferences[SHOW_SEARCH_HISTORY_KEY] ?: DEFAULT_SETTINGS_STATE.showSearchHistory
            )
        }
}