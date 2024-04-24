package com.kuss.krude.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BlurOff
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.R
import com.kuss.krude.db.AppInfo
import com.kuss.krude.ui.components.AppItem
import com.kuss.krude.ui.components.SoftKeyboardView
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import timber.log.Timber


@Composable
fun BottomSearchBar(
    mainViewModel: MainViewModel,
    settingsViewModel: SettingsViewModel,
    openApp: (AppInfo) -> Unit,
    toAppDetail: (AppInfo) -> Unit

) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val uiState by mainViewModel.state.collectAsState()
    val settingState by settingsViewModel.state.collectAsState()

    val currentStarPackageNameSet = uiState.currentStarPackageNameSet
    val apps = uiState.apps
    val filtering = uiState.filtering
    val filteredApps = uiState.filteredApps

    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember {
        FocusRequester()
    }

    val searchResultList = rememberLazyListState()

    var starMode by remember {
        mutableStateOf(false)
    }

    val isFocused = remember {
        mutableStateOf(false)
    }

    var selection by remember {
        mutableStateOf(TextRange(filtering.length))
    }

    val searchKeywordHistory = remember {
        mutableStateListOf<String>()
    }

    fun insertSearchHistory(text: String) {
        searchKeywordHistory.removeIf {
            it == text
        }
        searchKeywordHistory.add(0, text)
    }

    fun onTextChange(value: TextFieldValue) {
        starMode = false
        mainViewModel.filterApps(apps, value.text, settingState.fuzzySearch)
        mainViewModel.filterKeywordStars(context = context, value.text)
        selection = value.selection
    }

    fun refresh(fuzzy: Boolean) {
        mainViewModel.filterApps(apps, filtering, fuzzy)
        if (filtering.isNotEmpty()) {
            mainViewModel.filterKeywordStars(context = context, filtering)
        }
    }

    LaunchedEffect(apps.isNotEmpty(), settingState.autoFocus) {
        if (apps.isNotEmpty() && settingState.autoFocus) {
            focusRequester.requestFocus()
        } else if (!settingState.autoFocus) {
            focusRequester.freeFocus()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(filteredApps) {
        coroutineScope.launch {
            searchResultList.animateScrollToItem(0)
        }
    }

    AnimatedVisibility(
        visible = filtering.isNotEmpty(),
    ) {
        HorizontalDivider()

        Column {
            val hasMatch = filteredApps.isNotEmpty();
            AnimatedVisibility(visible = starMode && hasMatch) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.TwoTone.Star,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Star",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacing(x = 1)
                        Text(
                            text = filtering,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacing(x = 1)
                        Text(text = "to app", color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            Crossfade(targetState = hasMatch, label = "filteredItems") {
                val height = 128.dp
                if (it) {
                    LazyRow(
                        modifier = Modifier
                            .height(height)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        state = searchResultList,

                        ) {
                        itemsIndexed(
                            filteredApps,
                            key = { _, item -> item.packageName }) { _, item ->
                            val isStar = currentStarPackageNameSet.contains(item.packageName)
                            AppItem(
                                modifier = Modifier
                                    .width(96.dp),
                                item = item,
                                titleFontSize = 14.sp,
                                showStar = isStar,
                                titleSingleLine = true,
                                showSubtitle = false,
                                onClick = {
                                    if (starMode) {
                                        Timber.d("star $item")
                                        mainViewModel.starApp(
                                            context,
                                            item.packageName,
                                            keyword = filtering,
                                            isStar
                                        )
                                    } else {
                                        openApp(item)
                                        insertSearchHistory(filtering)
                                    }
                                },
                                onLongClick = {
                                    toAppDetail(item)
                                },
                                showTimes = settingState.showUsageCount,
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
    }

    HorizontalDivider()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(visible = filtering.isNotEmpty()) {
            IconButton(onClick = {
                mainViewModel.setFiltering("")
                selection = TextRange(0)
            }) {
                Icon(
                    Icons.Filled.Clear,
                    contentDescription = "Clear",
                    modifier = Modifier.size(ButtonDefaults.IconSize),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
        CompositionLocalProvider(LocalTextInputService provides if (settingState.useEmbedKeyboard) null else LocalTextInputService.current) {
            TextField(
                enabled = apps.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                    },
                value = TextFieldValue(filtering, selection),
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
                onValueChange = {
                    onTextChange(it)
                },
                placeholder = { Text(text = stringResource(id = R.string.search_placeholder)) },
            )
        }

        Box(
            modifier = Modifier
                .wrapContentSize(Alignment.TopStart)
        ) {
            Row {
                AnimatedVisibility(visible = filtering.isNotEmpty() && filteredApps.isNotEmpty()) {
                    IconButton(onClick = {
                        starMode = !starMode
                    }) {
                        Icon(
                            Icons.TwoTone.Star,
                            tint = MaterialTheme.colorScheme.primary,
                            contentDescription = "Star",
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                    }
                }
                IconButton(onClick = {
                    val nextFuzzy = !settingState.fuzzySearch
                    settingsViewModel.setFuzzySearch(nextFuzzy)
                    refresh(nextFuzzy)
                }) {
                    Icon(
                        imageVector = if (settingState.fuzzySearch) Icons.Filled.BlurOn else Icons.Filled.BlurOff,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "fuzzysearch",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
                IconButton(onClick = { mainViewModel.setShowMoreSheet(true) }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        tint = MaterialTheme.colorScheme.primary,
                        contentDescription = "MoreVert",
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                }
            }

            MoreModal(refresh = { refresh(settingState.fuzzySearch) }, mainViewModel = mainViewModel, settingsViewModel = settingsViewModel)

            AppUsageModal(mainViewModel)
        }

    }

    AnimatedVisibility(
        visible = settingState.useEmbedKeyboard && isFocused.value,
        enter = slideInVertically() + expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        SoftKeyboardView(onBack = {
            isFocused.value = false
            focusManager.clearFocus()
        }) { key, isDeleting ->
            val sb = StringBuilder(filtering)
            var range = selection
            if (isDeleting) {
                if (selection.end > 0) {
                    sb.deleteCharAt(selection.end - 1)
                    range = TextRange(selection.end - 1)
                }
            } else {
                if (selection.end < filtering.length)
                    sb.insert(selection.end, key)
                else
                    sb.append(key)
                range = TextRange(selection.end + 1)
            }
            onTextChange(TextFieldValue(sb.toString(), selection = range))
        }

        AnimatedVisibility(visible = settingState.showSearchHistory && searchKeywordHistory.size > 0) {
            LazyRow(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(16.dp)
            ) {
                item {
                    AnimatedVisibility(visible = searchKeywordHistory.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchKeywordHistory.clear()
                            }) {
                            Icon(
                                Icons.TwoTone.Delete,
                                tint = MaterialTheme.colorScheme.primary,
                                contentDescription = "delete",
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                        }
                    }
                }
                items(searchKeywordHistory) {
                    TextButton(onClick = {
                        onTextChange(TextFieldValue(it, TextRange(it.length)))
                        insertSearchHistory(it)
                    })
                    {
                        Text(
                            text = it,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                        )
                    }
                }
            }
        }
    }

}

