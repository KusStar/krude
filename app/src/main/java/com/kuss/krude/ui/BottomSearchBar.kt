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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
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
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.ui.components.AppItem
import com.kuss.krude.ui.components.CloseBtn
import com.kuss.krude.ui.components.ExtensionList
import com.kuss.krude.ui.components.MoreBtns
import com.kuss.krude.ui.components.SoftKeyboardView
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.utils.ExtensionHelper
import com.kuss.krude.utils.Reverse
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.DominantHandDefaults
import com.kuss.krude.viewmodel.settings.ExtensionDisplayModeDefaults
import com.kuss.krude.viewmodel.settings.SettingsState
import com.kuss.krude.viewmodel.settings.SettingsViewModel
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
    val settingsState by settingsViewModel.state.collectAsState()

    val currentStarPackageNameSet = uiState.currentStarPackageNameSet
    val apps = uiState.apps
    val searchResult = uiState.searchResult


    val coroutineScope = rememberCoroutineScope()

    val focusRequester = remember {
        FocusRequester()
    }

    val searchMainListState = rememberLazyListState()
    val searchExtensionListState = rememberLazyListState()

    var starMode by remember {
        mutableStateOf(false)
    }

    val isFocused = remember {
        mutableStateOf(false)
    }

    val searchKeywordHistory = remember {
        mutableStateListOf<String>()
    }
    var searchState by remember { mutableStateOf(TextFieldValue("")) }

    fun insertSearchHistory(text: String) {
        searchKeywordHistory.removeIf {
            it == text
        }
        searchKeywordHistory.add(0, text)
    }

    fun onTextChange(value: TextFieldValue) {
        searchState = value
        starMode = false
        mainViewModel.onSearch(value.text, settingsState.enableExtension, settingsState.fuzzySearch)
        mainViewModel.filterKeywordStars(
            context = context,
            settingsState.enableExtension,
            value.text
        )
    }

    fun refresh(fuzzy: Boolean) {
        mainViewModel.onSearch(searchState.text, settingsState.enableExtension, fuzzy)
        if (searchState.text.isNotEmpty()) {
            mainViewModel.filterKeywordStars(
                context = context,
                settingsState.enableExtension,
                searchState.text
            )
        }
    }

    fun clear() {
        searchState = TextFieldValue("")
    }

    fun onExtensionClick(extension: Extension, isStar: Boolean) {
        if (starMode) {
            Timber.d("star $extension")
            mainViewModel.starApp(
                context,
                settingsState.enableExtension,
                extension.name,
                keyword = searchState.text,
                isStar
            )
        } else {
            ExtensionHelper.launchExtension(context, extension)
            mainViewModel.updateExtensionPriority(extension)
            clear()
        }
    }

    fun onAppClick(app: AppInfo, isStar: Boolean) {
        if (starMode) {
            Timber.d("star $app")
            mainViewModel.starApp(
                context,
                settingsState.enableExtension,
                app.packageName,
                keyword = searchState.text,
                isStar
            )
        } else {
            openApp(app)
            insertSearchHistory(searchState.text)
            searchState = TextFieldValue("")
        }
    }

    LaunchedEffect(apps) {
        refresh(settingsState.fuzzySearch)
    }

    LaunchedEffect(apps.isNotEmpty(), settingsState.autoFocus) {
        if (apps.isNotEmpty() && settingsState.autoFocus) {
            focusRequester.requestFocus()
        } else if (!settingsState.autoFocus) {
            focusRequester.freeFocus()
            focusManager.clearFocus()
        }
    }

    LaunchedEffect(searchResult) {
        coroutineScope.launch {
            searchMainListState.animateScrollToItem(0)
            searchExtensionListState.animateScrollToItem(0)
        }
    }

    LaunchedEffect(settingsState.enableExtension, uiState.extensionMap) {
        coroutineScope.launch {
            refresh(settingsState.fuzzySearch)
        }
    }

    AnimatedVisibility(
        visible = searchState.text.isNotEmpty(),
    ) {
        HorizontalDivider()
        Crossfade(targetState = searchResult.isNotEmpty(), label = "searchList") { show ->
            if (show) {
                Column {
                    AnimatedVisibility(visible = starMode && searchResult.isNotEmpty()) {
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
                                    text = searchState.text,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacing(x = 1)
                                Text(text = "to app", color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                    }

                    if (settingsState.enableExtension && settingsState.extensionDisplayMode == ExtensionDisplayModeDefaults.ON_TOP) {
                        val hasApp = searchResult.any { it.isApp() }
                        ExtensionList(
                            searchResult = searchResult,
                            listState = searchExtensionListState,
                            starSet = currentStarPackageNameSet,
                            showUsageCount = settingsState.showUsageCount,
                            onExtensionClick = { extension, isStar ->
                                onExtensionClick(extension, isStar)
                            },
                            settingsState.dominantHand == DominantHandDefaults.RIGHT,
                            settingsState.extensionGroupLayout
                        )
                        AnimatedVisibility(visible = hasApp) {
                            HorizontalDivider()
                        }
                    }

                    MainList(
                        searchResult = searchResult,
                        listState = searchMainListState,
                        starSet = currentStarPackageNameSet,
                        settingsState = settingsState,
                        onAppClick = { app, isStar ->
                            onAppClick(app, isStar)
                        },
                        toAppDetail = { app ->
                            toAppDetail(app)
                        },
                        reverseLayout = settingsState.dominantHand == DominantHandDefaults.RIGHT
                    )

                    if (settingsState.enableExtension && settingsState.extensionDisplayMode == ExtensionDisplayModeDefaults.ON_BOTTOM) {
                        val hasApp = searchResult.any { it.isApp() }
                        AnimatedVisibility(visible = hasApp) {
                            HorizontalDivider()
                        }
                        ExtensionList(
                            searchResult = searchResult,
                            listState = searchExtensionListState,
                            starSet = currentStarPackageNameSet,
                            showUsageCount = settingsState.showUsageCount,
                            onExtensionClick = { extension, isStar ->
                                onExtensionClick(extension, isStar)
                            },
                            settingsState.dominantHand == DominantHandDefaults.RIGHT,
                            settingsState.extensionGroupLayout
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
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

    HorizontalDivider()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = if (settingsState.dominantHand == DominantHandDefaults.LEFT) Arrangement.Center else Arrangement.Reverse
    ) {
        CloseBtn(visible = searchState.text.isNotEmpty()) {
            clear()
        }

        CompositionLocalProvider(LocalTextInputService provides if (settingsState.useEmbedKeyboard) null else LocalTextInputService.current) {
            TextField(
                enabled = apps.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .focusRequester(focusRequester)
                    .onFocusChanged {
                        isFocused.value = it.isFocused
                    },
                value = searchState,
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

        MoreBtns(
            search = searchState.text,
            searchResult = searchResult,
            fuzzySearch = settingsState.fuzzySearch,
            onStarIcon = {
                starMode = !starMode
            },
            onFuzzyIcon = {
                val nextFuzzy = !settingsState.fuzzySearch
                settingsViewModel.setFuzzySearch(nextFuzzy)
                refresh(nextFuzzy)
            },
            onMoreIcon = {
                mainViewModel.setShowMoreSheet(true)
            },
            dominantHand = settingsState.dominantHand
        )
    }

    AnimatedVisibility(
        visible = settingsState.useEmbedKeyboard && isFocused.value,
        enter = slideInVertically() + expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
        exit = slideOutVertically() + shrinkVertically() + fadeOut()
    ) {
        SoftKeyboardView(
            showLeftSideBackspace = settingsState.showLeftSideBackSpace,
            onBack = {
                isFocused.value = false
                focusManager.clearFocus()
            }, onClick = { key, isDeleting ->
                val sb = StringBuilder(searchState.text)
                var range = searchState.selection
                if (isDeleting) {
                    if (range.end > 0) {
                        sb.deleteCharAt(range.end - 1)
                        range = TextRange(range.end - 1)
                    }
                } else {
                    if (range.end < searchState.text.length)
                        sb.insert(range.end, key)
                    else
                        sb.append(key)
                    range = TextRange(range.end + 1)
                }
                onTextChange(TextFieldValue(sb.toString(), selection = range))
            }) {
            AnimatedVisibility(visible = settingsState.showSearchHistory && searchKeywordHistory.size > 0) {
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
                    items(searchKeywordHistory, key = { it }) {
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

    MoreModal(
        refresh = { refresh(settingsState.fuzzySearch) },
        mainViewModel = mainViewModel,
        settingsViewModel = settingsViewModel
    )

    AppUsageModal(mainViewModel)
}

@Composable
fun MainList(
    searchResult: List<SearchResultItem>,
    listState: LazyListState,
    starSet: Set<String>,
    settingsState: SettingsState,
    onAppClick: (app: AppInfo, isStar: Boolean) -> Unit,
    toAppDetail: (AppInfo) -> Unit,
    reverseLayout: Boolean
) {
    val mainData = remember(settingsState, searchResult) {
        searchResult.filter { it.isApp() }
    }
    AnimatedVisibility(visible = mainData.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            state = listState,
            reverseLayout = reverseLayout
        ) {
            itemsIndexed(
                mainData,
                key = { _, item -> item.key() }) { _, item ->
                val app = item.asApp()!!
                val isStar = starSet.contains(app.packageName)
                AppItem(
                    modifier = Modifier
                        .width(96.dp),
                    item = app,
                    titleFontSize = 14.sp,
                    showStar = isStar,
                    titleSingleLine = true,
                    showSubtitle = false,
                    onClick = {
                        onAppClick(app, isStar)
                    },
                    onLongClick = {
                        toAppDetail(app)
                    },
                    showTimes = settingsState.showUsageCount,
                )
            }
        }
    }
}

