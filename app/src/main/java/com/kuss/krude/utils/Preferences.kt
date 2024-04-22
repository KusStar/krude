package com.kuss.krude.utils

import androidx.compose.runtime.Composable
import com.alorma.compose.settings.storage.preferences.BooleanPreferenceSettingValueState
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState

private const val KEY_AUTO_FOCUS = "auto_focus"
private const val KEY_FUZZY_SEARCH = "fuzzy_search"
private const val KEY_SHOW_USAGE_COUNT = "show_usage_count"
private const val KEY_SHOW_EMBED_KEYBOARD = "embed_keyboard"


@Composable
fun useAutoFocus(): BooleanPreferenceSettingValueState {
    return rememberPreferenceBooleanSettingState(key = KEY_AUTO_FOCUS, defaultValue = true)
}

@Composable
fun useFuzzySearch(): BooleanPreferenceSettingValueState {
    return rememberPreferenceBooleanSettingState(key = KEY_FUZZY_SEARCH, defaultValue = true)
}

@Composable
fun useShowUsageCount(): BooleanPreferenceSettingValueState {
    return rememberPreferenceBooleanSettingState(key = KEY_SHOW_USAGE_COUNT, defaultValue = false)
}


@Composable
fun useEmbedKeyboard(): BooleanPreferenceSettingValueState {
    return rememberPreferenceBooleanSettingState(key = KEY_SHOW_EMBED_KEYBOARD, defaultValue = false)
}