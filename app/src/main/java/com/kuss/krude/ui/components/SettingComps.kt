package com.kuss.krude.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalLeft
import androidx.compose.material.icons.automirrored.filled.AlignHorizontalRight
import androidx.compose.material.icons.automirrored.filled.KeyboardBackspace
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Segment
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.HistoryToggleOff
import androidx.compose.material.icons.filled.ImportantDevices
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.KeyboardCommandKey
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsCheckbox
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.kuss.krude.R
import com.kuss.krude.utils.ModalSheetModifier
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.DEFAULT_SETTINGS_STATE
import com.kuss.krude.viewmodel.settings.DominantHandDefaults
import com.kuss.krude.viewmodel.settings.ExtensionDisplayModeDefaults
import com.kuss.krude.viewmodel.settings.SettingsViewModel
import me.zhanghai.compose.preference.ListPreference
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.SliderPreference

@Composable
fun SettingSections(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel, dismiss: () -> Unit
) {
    val context = LocalContext.current
    val settingsState by settingsViewModel.state.collectAsState()

    var showEmbedKeyboardSettings by remember {
        mutableStateOf(false)
    }

    // settingsState.dominateHand
    ProvidePreferenceTheme {
        ListPreference(
            value = settingsState.dominantHand,
            onValueChange = {
                settingsViewModel.setDominantHand(it)
            },
            values = listOf(DominantHandDefaults.LEFT, DominantHandDefaults.RIGHT),
            title = { Text(text = stringResource(id = R.string.dominant_hand)) },
            modifier = Modifier.fillMaxWidth(),
            icon = {
                Icon(imageVector = Icons.AutoMirrored.Default.let {
                    return@let if (settingsState.dominantHand == DominantHandDefaults.LEFT) {
                        it.AlignHorizontalLeft
                    } else {
                        it.AlignHorizontalRight
                    }
                }, contentDescription = null)
            },
            summary = { Text(text = settingsState.dominantHand) }
        )
    }

    // settingsState.autoFocus
    SettingsCheckbox(
        icon = {
            Icon(
                imageVector = Icons.Default.CenterFocusWeak,
                contentDescription = stringResource(id = R.string.auto_focus)
            )
        },
        title = { Text(text = stringResource(id = R.string.auto_focus)) },
        state = settingsState.autoFocus,
        onCheckedChange = { next ->
            settingsViewModel.setAutoFocus(next)
        }
    )

//  extension settings
    OutlinedCard(
        border = if (settingsState.enableExtension) CardDefaults.outlinedCardBorder() else BorderStroke(
            1.dp,
            Color.Transparent
        ),
    ) {
        // settingsState.enableExtension
        SettingsCheckbox(
            icon = {
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = stringResource(id = R.string.enable_extension)
                )
            },
            title = { Text(text = stringResource(id = R.string.enable_extension)) },
            state = settingsState.enableExtension,
            onCheckedChange = { next ->
                settingsViewModel.setEnableExtension(next)
            }
        )

        AnimatedVisibility(visible = settingsState.enableExtension) {
            Column {
                // settingsState.extensionDisplayMode
                ProvidePreferenceTheme {
                    ListPreference(
                        value = settingsState.extensionDisplayMode,
                        onValueChange = {
                            settingsViewModel.setExtensionDisplayMode(it)
                        },
                        values = listOf(
                            ExtensionDisplayModeDefaults.ON_TOP,
                            ExtensionDisplayModeDefaults.ON_BOTTOM
                        ),
                        title = { Text(text = stringResource(id = R.string.extension_display_mode)) },
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.Segment,
                                contentDescription = null
                            )
                        },
                        summary = { Text(text = settingsState.extensionDisplayMode) }
                    )
                }
                // settingsState.extensionGroupLayout
                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Merge,
                            contentDescription = null
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.extension_group_layout)) },
                    state = settingsState.extensionGroupLayout,
                    onCheckedChange = { next ->
                        settingsViewModel.setExtensionGroupLayout(next)
                    }
                )
                // refetch extensions
                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(id = R.string.refetch_extensions)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.refetch_extensions)) },
                    onClick = {
                        mainViewModel.clearExtensionsCache(context)
                        mainViewModel.loadExtensions(context)
                    }
                )
                // extension list
                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(id = R.string.extension_list)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.extension_list)) },
                    onClick = {

                    }
                )
                if (settingsState.devMode) {
                    // settingsState.devExtension
                    SettingsCheckbox(
                        icon = {
                            Icon(
                                imageVector = Icons.Default.ImportantDevices,
                                contentDescription = null
                            )
                        },
                        title = { Text(text = "Use dev extension repo") },
                        subtitle = { Text(settingsState.devExtensionRepo) },
                        state = settingsState.devExtension,
                        onCheckedChange = { next ->
                            settingsViewModel.setDevExtension(next)
                        }
                    )
                }
            }
        }
    }
//  extension settings spacing
    if (settingsState.enableExtension || settingsState.useEmbedKeyboard) {
        Spacing(x = 1)
    }
//  keyboard settings
    OutlinedCard(
        border = if (settingsState.useEmbedKeyboard) CardDefaults.outlinedCardBorder() else BorderStroke(
            1.dp,
            Color.Transparent
        )
    ) {
        // settingsState.useEmbedKeyboard
        SettingsCheckbox(
            icon = {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = stringResource(id = R.string.embed_keyboard)
                )
            },
            title = { Text(text = stringResource(id = R.string.embed_keyboard)) },
            state = settingsState.useEmbedKeyboard,
            onCheckedChange = { next ->
                settingsViewModel.setUseEmbedKeyboard(next)
            }
        )

        AnimatedVisibility(visible = settingsState.useEmbedKeyboard) {
            Column {
                // settingsState.showLeftSideBackSpace
                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardBackspace,
                            contentDescription = stringResource(id = R.string.show_left_side_backspace)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.show_left_side_backspace)) },
                    state = settingsState.showLeftSideBackSpace,
                    onCheckedChange = { next ->
                        settingsViewModel.setShowLeftSideBackspace(next)
                    },
                )
                // settingsState.showSearchHistory
                SettingsCheckbox(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.HistoryToggleOff,
                            contentDescription = stringResource(id = R.string.show_search_history)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.show_search_history)) },
                    state = settingsState.showSearchHistory,
                    onCheckedChange = { next ->
                        settingsViewModel.setShowSearchHistory(next)
                    }
                )
                // go to embed keyboard settings
                SettingsMenuLink(
                    icon = {
                        Icon(
                            imageVector = Icons.Default.KeyboardCommandKey,
                            contentDescription = stringResource(id = R.string.embed_keyboard_settings)
                        )
                    },
                    title = { Text(text = stringResource(id = R.string.embed_keyboard_settings)) },
                    onClick = {
                        showEmbedKeyboardSettings = true
                    },
                )
            }
        }
    }
    // settingsState.showUsageCount
    SettingsCheckbox(
        icon = {
            Icon(
                imageVector = Icons.Default.Numbers,
                contentDescription = stringResource(id = R.string.show_usage_count)
            )
        },
        title = { Text(text = stringResource(id = R.string.show_usage_count)) },
        state = settingsState.showUsageCount,
        onCheckedChange = { next ->
            settingsViewModel.setShowUsageCount(next)
        }
    )

    if (showEmbedKeyboardSettings) {
        EmbedKeyboardSettings(settingsViewModel, onDismiss = {
            showEmbedKeyboardSettings = false
        })
    }
}

data class KeyboardSettingPreset(
    val label: String,
    val scale: Float,
    val offset: Float,
)

val KeyboardSettingPresets = listOf(
    KeyboardSettingPreset("Left", 0.9f, -16f),
    KeyboardSettingPreset("Center", 1f, 0f),
    KeyboardSettingPreset("Right", 0.9f, 16f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmbedKeyboardSettings(settingsViewModel: SettingsViewModel, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            onDismiss()
        },
        sheetState = sheetState,
        modifier = ModalSheetModifier
    ) {
        var scale by remember {
            mutableFloatStateOf(1f)
        }
        var offset by remember {
            mutableFloatStateOf(0f)
        }

        var presetChecked by remember {
            mutableIntStateOf(1)
        }
        val clicked = remember {
            mutableStateListOf<String>()
        }
        val textScrollState = rememberScrollState()
        LaunchedEffect(Unit) {
            scale = settingsViewModel.state.value.customKeyboardScale
            offset = settingsViewModel.state.value.customKeyboardOffset.toFloat()
            if (scale != DEFAULT_SETTINGS_STATE.customKeyboardScale && offset.toInt() != DEFAULT_SETTINGS_STATE.customKeyboardOffset) {
                presetChecked = -1
            }
        }
        LaunchedEffect(clicked.size) {
            textScrollState.animateScrollTo(textScrollState.maxValue)
        }
        LaunchedEffect(offset, scale) {
            settingsViewModel.setCustomKeyboardScale(scale)
            settingsViewModel.setCustomKeyboardOffset(offset.toInt())
        }

        Box(modifier = Modifier.weight(1f, true))
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            AnimatedVisibility(clicked.size > 0) {
                Card {
                    Text(
                        text = clicked.joinToString(""),
                        modifier = Modifier
                            .padding(12.dp)
                            .verticalScroll(textScrollState)
                            .animateContentSize()
                    )
                }
            }
        }
        Spacing(1)
        SoftKeyboardView(
            true,
            onBack = {
                onDismiss()
            },
            onClick = { key, isDeleting ->
                clicked.add(if (!isDeleting) key.toString() else "\uD83D\uDD19")
            },
            scale = scale,
            offset = offset.toInt()
        ) {}
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            MultiChoiceSegmentedButtonRow {
                KeyboardSettingPresets.forEachIndexed { index, preset ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = KeyboardSettingPresets.size
                        ),
                        onCheckedChange = {
                            presetChecked = index
                            scale = preset.scale
                            offset = preset.offset
                        },
                        checked = index == presetChecked
                    ) {
                        Text(preset.label)
                    }
                }
            }
        }
        ProvidePreferenceTheme {
            SliderPreference(
                title = { Text(text = "Keyboard Scale") },
                value = scale,
                valueRange = 0.1f..2f,
                onValueChange = {
                    scale = it
                },
                sliderValue = scale,
                onSliderValueChange = {
                    scale = it
                },
                summary = { Text(text = "%.2f".format(scale)) }
            )
            SliderPreference(
                title = { Text(text = "Keyboard Offset") },
                value = offset,
                valueRange = -100f..100f,
                onValueChange = {
                    offset = it
                },
                sliderValue = offset,
                onSliderValueChange = {
                    offset = it
                },
                summary = { Text(text = offset.toInt().toString()) }
            )
        }
        Spacing(2)
    }
}
