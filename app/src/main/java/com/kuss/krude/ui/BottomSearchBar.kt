package com.kuss.krude.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Star
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kuss.krude.R
import com.kuss.krude.db.AppInfo
import com.kuss.krude.ui.components.internal.InternalExtensions
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.interfaces.SearchResultType
import com.kuss.krude.ui.components.MessageBar
import com.kuss.krude.ui.components.ScrollWheel
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.internal.SecondLevelArea
import com.kuss.krude.ui.components.rememberMessageBarState
import com.kuss.krude.ui.components.rememberScrollWheelState
import com.kuss.krude.ui.components.search.CloseBtn
import com.kuss.krude.ui.components.search.ExtensionDropdownType
import com.kuss.krude.ui.components.search.ExtensionList
import com.kuss.krude.ui.components.search.MainList
import com.kuss.krude.ui.components.search.MoreBtns
import com.kuss.krude.ui.components.search.SoftKeyboardView
import com.kuss.krude.utils.ExtensionHelper
import com.kuss.krude.utils.Reverse
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.DominantHandDefaults
import com.kuss.krude.viewmodel.settings.ExtensionDisplayModeDefaults
import com.kuss.krude.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.delay
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

    val starSet = uiState.currentStarPackageNameSet
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

    // second level extension
    var inSecondLevel by remember { mutableStateOf(false) }
    var secondLevelExtension by remember {
        mutableStateOf<Extension?>(null)
    }

    val messageBarState = rememberMessageBarState()

    val scrollWheelState = rememberScrollWheelState()

    LaunchedEffect(Unit) {
        mainViewModel.initMessageBarState(messageBarState)
    }

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
        mainViewModel.clearSearch()
    }

    fun onExtensionClick(extension: Extension, isStar: Boolean, isFreeformWindow: Boolean = false) {
        if (starMode) {
            Timber.d("star $extension")
            mainViewModel.insertStar(
                context,
                settingsState.enableExtension,
                extension.id,
                keyword = searchState.text,
                isStar
            )
            starMode = !starMode
        } else {
            if (extension.type == ExtensionType.INTERNAL) {
                if (InternalExtensions.IDS.contains(extension.id)) {
                    inSecondLevel = true
                    secondLevelExtension = extension
                }
            } else {
                ExtensionHelper.launchExtension(context, extension, isFreeformWindow)
            }
            mainViewModel.updateExtensionPriority(extension)
            clear()
        }
    }

    fun onAppClick(app: AppInfo, isStar: Boolean) {
        if (starMode) {
            Timber.d("star $app")
            mainViewModel.insertStar(
                context,
                settingsState.enableExtension,
                app.packageName,
                keyword = searchState.text,
                isStar
            )
            starMode = !starMode
        } else {
            openApp(app)
            insertSearchHistory(searchState.text)
            searchState = TextFieldValue("")
        }
    }

    fun onExtensionDropdown(extension: Extension, type: ExtensionDropdownType) {
        when (type) {
            ExtensionDropdownType.STAR -> {
                mainViewModel.setStarItemDialogVisible(
                    true,
                    SearchResultItem(
                        type = SearchResultType.EXTENSION,
                        extension = extension
                    )
                )
            }

            ExtensionDropdownType.OPEN_IN_FREEFORM_WINDOW -> {
                starMode = false
                onExtensionClick(extension, isStar = false, isFreeformWindow = true)
            }
        }
    }

    val lifeCycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifeCycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            Timber.d("Lifecycle event: $event")
            if (event == Lifecycle.Event.ON_DESTROY) {
                mainViewModel.unregisterPackageEventReceiver(context)
            }
            if (event == Lifecycle.Event.ON_RESUME) {
                coroutineScope.launch {
                    delay(100)
                    focusRequester.requestFocus()
                    isFocused.value = true
                }
                if (searchState.text.isEmpty()) {
                    mainViewModel.reloadAppsFromSystem(context)
                }
            }
        }
        lifeCycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifeCycleOwner.lifecycle.removeObserver(observer)
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

    LaunchedEffect(inSecondLevel) {
        focusRequester.requestFocus()
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

    BackHandler(enabled = inSecondLevel) {
        inSecondLevel = false
    }

    MessageBar(state = messageBarState)

    AnimatedContent(targetState = inSecondLevel, label = "level") { displayLevel ->
        if (displayLevel) {
            SecondLevelArea(
                onBack = {
                    inSecondLevel = false
                },
                data = secondLevelExtension,
                focusRequester = focusRequester
            )
        } else {
            Column {
                AnimatedVisibility(
                    visible = searchState.text.isNotEmpty(),
                ) {
                    HorizontalDivider()
                    Crossfade(
                        targetState = searchResult.isNotEmpty(),
                        label = "searchList",
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                    ) { show ->
                        if (show) {
                            Column {
                                AnimatedVisibility(
                                    visible = starMode && searchResult.isNotEmpty()
                                ) {
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
                                                text = "\"",
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Text(
                                                text = searchState.text,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "\"",
                                                color = MaterialTheme.colorScheme.secondary
                                            )
                                            Spacing(x = 1)
                                            Text(
                                                text = "to",
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                if (settingsState.enableExtension && settingsState.extensionDisplayMode == ExtensionDisplayModeDefaults.ON_TOP) {
                                    val hasApp = searchResult.any { it.isApp() }
                                    ExtensionList(
                                        scrollWheelState = scrollWheelState,
                                        searchResult = searchResult,
                                        listState = searchExtensionListState,
                                        starSet = starSet,
                                        showUsageCount = settingsState.showUsageCount,
                                        onExtensionClick = { extension, isStar ->
                                            onExtensionClick(extension, isStar = isStar)
                                        },
                                        settingsState.dominantHand == DominantHandDefaults.RIGHT,
                                        settingsState.extensionGroupLayout,
                                        onDropdown = { extension, type ->
                                            onExtensionDropdown(extension, type)
                                        },
                                    )
                                    AnimatedVisibility(visible = hasApp) {
                                        HorizontalDivider()
                                    }
                                }

                                MainList(
                                    searchResult = searchResult,
                                    listState = searchMainListState,
                                    starSet = starSet,
                                    settingsState = settingsState,
                                    onAppClick = { app, isStar ->
                                        onAppClick(app, isStar)
                                    },
                                    toAppDetail = { app ->
                                        toAppDetail(app)
                                    },
                                    reverseLayout = settingsState.dominantHand == DominantHandDefaults.RIGHT,
                                    onExtensionClick = { extension, isStar ->
                                        onExtensionClick(extension, isStar)
                                    },
                                    onExtensionDropdown = { extension, type ->
                                        onExtensionDropdown(extension, type)
                                    }
                                )

                                if (settingsState.enableExtension && settingsState.extensionDisplayMode == ExtensionDisplayModeDefaults.ON_BOTTOM) {
                                    val hasApp = searchResult.any { it.isApp() }
                                    AnimatedVisibility(visible = hasApp) {
                                        HorizontalDivider()
                                    }
                                    ExtensionList(
                                        scrollWheelState = scrollWheelState,
                                        searchResult = searchResult,
                                        listState = searchExtensionListState,
                                        starSet = starSet,
                                        showUsageCount = settingsState.showUsageCount,
                                        onExtensionClick = { extension, isStar ->
                                            onExtensionClick(extension, isStar)
                                        },
                                        settingsState.dominantHand == DominantHandDefaults.RIGHT,
                                        settingsState.extensionGroupLayout,
                                        onDropdown = { extension, type ->
                                            onExtensionDropdown(extension, type)
                                        },
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
                        dominantHand = settingsState.dominantHand,
                        onExtensionClick = { extension, isStar ->
                            onExtensionClick(extension, isStar)
                        },
                        onAppClick = { app, isStar ->
                            onAppClick(app, isStar)
                        },
                        starSet = starSet,
                    )
                }
                AnimatedVisibility(
                    visible = settingsState.useEmbedKeyboard && isFocused.value,
                    enter = slideInVertically() + expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                    exit = slideOutVertically() + shrinkVertically() + fadeOut()
                ) {
                    SoftKeyboardView(
                        showLeftSideBackspace = settingsState.showLeftSideBackSpace,
                        scale = settingsState.customKeyboardScale,
                        offset = settingsState.customKeyboardOffset,
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
                        Spacing(x = 2)
                        val wheelCount = remember(searchResult) {
                            searchResult.filter { it.isExtension() }.size
                        }
                        if (wheelCount > 0) {
                            ScrollWheel(count = wheelCount, state = scrollWheelState)
                        } else {
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                        Spacing(x = 1)
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


