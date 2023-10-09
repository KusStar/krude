package com.kuss.krude.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alorma.compose.settings.storage.preferences.rememberPreferenceBooleanSettingState
import com.kuss.krude.R
import com.kuss.krude.data.AppInfo
import com.kuss.krude.viewmodel.MainViewModel


@Composable
fun BottomSearchBar(
    mainViewModel: MainViewModel,
    openApp: (AppInfo) -> Unit,
    toAppDetail: (AppInfo) -> Unit
) {
    val context = LocalContext.current
    val uiState by mainViewModel.state.collectAsState()
    val apps = uiState.apps
    val filtering = uiState.filtering
    val filteredApps = uiState.filteredApps
    val scope = rememberCoroutineScope()
    val focusRequester = remember {
        FocusRequester()
    }

    val autoFocus = rememberPreferenceBooleanSettingState(key = "auto_focus", defaultValue = true)
    val fuzzySearch = rememberPreferenceBooleanSettingState(key = "fuzzy_search", defaultValue = true)

    LaunchedEffect(apps.isNotEmpty(), autoFocus.value) {
        if (apps.isNotEmpty() && autoFocus.value) {
            focusRequester.requestFocus()
        } else if (!autoFocus.value) {
            focusRequester.freeFocus()
        }
    }

    AnimatedVisibility(
        visible = filtering.isNotEmpty(),
    ) {
        Divider()
        Crossfade(targetState = filteredApps.isNotEmpty(), label = "filteredItems") {
            val height = 108.dp
            if (it) {
                LazyRow(
                    modifier = Modifier
                        .height(height)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    itemsIndexed(filteredApps) { _, item ->
                        AppItem(
                            modifier = Modifier
                                .width(96.dp),
                            item = item,
                            titleFontSize = 14.sp,
                            titleSingleLine = true,
                            showSubtitle = false,
                            onClick = {
                                openApp(item)
                            },
                            onLongClick = {
                                toAppDetail(item)
                            }
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.mipmap.ic_launcher_foreground),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp)
                    )
                    Text(
                        text = stringResource(id = R.string.no_match_app),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }

    Divider()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = filtering.isNotEmpty()) {
            IconButton(onClick = { mainViewModel.setFiltering("") }) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
        }
        TextField(
            enabled = apps.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .focusRequester(focusRequester),
            value = filtering,
            singleLine = true,
            colors = TextFieldDefaults.colors(
                unfocusedTextColor = MaterialTheme.colorScheme.secondary,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                cursorColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = Color.Transparent,
                errorContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                focusedPlaceholderColor = MaterialTheme.colorScheme.primary
            ),
            onValueChange = { text ->
                mainViewModel.filterApps(apps, text, fuzzySearch.value)
            },
            placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
        )
        var expanded by remember { mutableStateOf(false) }

        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart)
        ) {
            Row {
                IconButton(onClick = {
                    fuzzySearch.value = !fuzzySearch.value
                    mainViewModel.filterApps(apps, filtering, fuzzySearch.value)
                }) {
                    Icon(
                        imageVector = if (fuzzySearch.value) Icons.Filled.BlurOn else Icons.Filled.BlurOff,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "fuzzysearch",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
                IconButton(onClick = { expanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "MoreVert",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = stringResource(id = R.string.reset_app_priority)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.reset_app_priority))
                        }
                    },
                    onClick = {
                        mainViewModel.resetDbAppsPriority(context)
                        expanded = false
                    })

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = stringResource(id = R.string.reload_apps)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.reload_apps))
                        }
                    },
                    onClick = {
                        mainViewModel.loadFromPackageManger(context = context)
                        expanded = false
                    })

                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CenterFocusWeak,
                                contentDescription = stringResource(id = R.string.auto_focus)
                            )
                            Spacing(x = 1)
                            Text(text = stringResource(id = R.string.auto_focus))
                            Spacing(x = 1)
                            Checkbox(checked = autoFocus.value, onCheckedChange = {
                                autoFocus.value = it
                            })
                        }
                    },
                    onClick = {
                        autoFocus.value = !autoFocus.value
                    })
            }
        }

    }
}

