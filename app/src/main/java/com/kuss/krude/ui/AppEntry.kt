package com.kuss.krude.ui

import android.app.Activity
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuss.krude.R
import com.kuss.krude.db.AppInfo
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.interfaces.SearchResultType
import com.kuss.krude.ui.components.AppDropdownType
import com.kuss.krude.ui.components.Spacing
import com.kuss.krude.ui.components.search.AppItem
import com.kuss.krude.ui.components.search.AsyncAppIcon
import com.kuss.krude.ui.components.search.ExtensionIcon
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.SearchHelper
import com.kuss.krude.viewmodel.MainViewModel
import com.kuss.krude.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings

@Composable
fun AppsList(
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel,
    openApp: (AppInfo) -> Unit,
    onAppDropdown: (AppInfo, AppDropdownType) -> Unit
) {
    val uiState by mainViewModel.state.collectAsState()
    val listState = rememberLazyStaggeredGridState()
    val apps = uiState.apps
    val scrollbarItems = uiState.scrollbarItems

    val firstVisibleItemIndex by remember {
        derivedStateOf { listState.firstVisibleItemIndex }
    }
    Row(modifier = modifier) {
        LaunchedEffect(firstVisibleItemIndex) {
            val next = scrollbarItems.indexOfFirst {
                it == apps[firstVisibleItemIndex + 1].abbr.first().uppercase()
            }

            mainViewModel.setSelectedHeaderIndex(next)
        }
        LazyVerticalStaggeredGrid(state = listState,
            columns = StaggeredGridCells.Adaptive(128.dp),
            // content padding
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(
                start = 12.dp, top = 16.dp, end = 12.dp, bottom = 12.dp
            ),
            content = {
                if (apps.isNotEmpty()) {
                    items(apps, key = { item -> item.packageName }) { item ->
                        AppItem(item = item, onClick = {
                            openApp(item)
                        }, onDropdown = { type ->
                            onAppDropdown(item, type)
                        })
                    }
                }
            })
        AlphabetScrollbar(mainViewModel = mainViewModel, listState = listState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppEntry(
    mainViewModel: MainViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val context = LocalContext.current
    val activity = LocalContext.current as Activity
    val focusManager = LocalFocusManager.current

    val uiState by mainViewModel.state.collectAsState()
    val missingPermission = uiState.missingPermission


    LaunchedEffect(Unit) {
        mainViewModel.initSettingsViewModel(settingsViewModel)

        mainViewModel.initPackageEventReceiver(context)

        mainViewModel.loadApps(context)
    }

    fun openApp(appInfo: AppInfo, isFreeformWindow: Boolean = false) {
        ActivityHelper.startPackageActivity(
            context,
            appInfo.packageName,
            isFreeformWindow = isFreeformWindow,
            view = activity.window.decorView
        )

        mainViewModel.recordOpenApp(context, appInfo)
    }

    fun onAppDropdown(app: AppInfo, type: AppDropdownType) {
        when (type) {
            AppDropdownType.STAR -> {
                mainViewModel.setStarItemDialogVisible(
                    true, item = SearchResultItem(
                        SearchResultType.APP, app = app
                    )
                )
            }

            AppDropdownType.OPEN_IN_FREEFORM_WINDOW -> {
                openApp(app, true)
            }

            AppDropdownType.APP_INFO -> {
                ActivityHelper.toDetail(context, app.packageName)
            }

            AppDropdownType.HIDE -> {
                mainViewModel.insertHidden(context, app.packageName)
                mainViewModel.setShowAppStatsModal(false)
            }

            AppDropdownType.UNINSTALL -> {
                ActivityHelper.toUninstall(context, app.packageName)
            }

            AppDropdownType.APP_STATS -> {
                mainViewModel.setShowAppStatsModal(true, app)

                focusManager.clearFocus()
            }
        }
    }

    if (!missingPermission) {
        Column {
            Column(modifier = Modifier.weight(1f, false)) {
                val scope = rememberCoroutineScope()
                val titles =
                    listOf(
                        stringResource(R.string.search),
                        stringResource(R.string.apps),
                        stringResource(R.string.extensions)
                    )
                var tabIndex by remember { mutableIntStateOf(0) }
                SecondaryTabRow(selectedTabIndex = tabIndex) {
                    titles.forEachIndexed { index, title ->
                        Tab(
                            selected = tabIndex == index,
                            onClick = {
                                scope.launch {
                                    tabIndex = index
                                }
                            },
                            text = {
                                Text(
                                    text = title, maxLines = 1, overflow = TextOverflow.Ellipsis
                                )
                            })
                    }
                }
                Crossfade(tabIndex) { index ->
                    when (index) {
                        0 -> {
                            Column(modifier = Modifier.fillMaxHeight()) {
                                val listState = rememberLazyListState()
                                val scope = rememberCoroutineScope()
                                var searchResult by remember {
                                    mutableStateOf<List<String>>(listOf())
                                }
                                val urlHandler = LocalUriHandler.current
                                LaunchedEffect(uiState.currentSearch) {
                                    scope.launch(IO) {
                                        if (uiState.currentSearch.isNotEmpty()) {
                                            SearchHelper.queryBing(uiState.currentSearch) { result ->
                                                searchResult = result
                                            }
                                        } else {
                                            searchResult = listOf()
                                        }
                                    }
                                }
                                LazyColumnScrollbar(
                                    state = listState,
                                    settings = ScrollbarSettings.Default.copy(
                                        thumbThickness = 6.dp,
                                        scrollbarPadding = 0.dp,
                                        thumbSelectedColor = MaterialTheme.colorScheme.primary,
                                        thumbUnselectedColor = MaterialTheme.colorScheme.secondary
                                    )
                                ) {
                                    LazyColumn(state = listState) {
                                        items(searchResult) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        scope.launch(IO) {
                                                            val intent = CustomTabsIntent.Builder().build()

                                                            intent.launchUrl(
                                                                context,
                                                                Uri.parse("https://cn.bing.com/search?q=${it}")
                                                            )
                                                        }
                                                    }
                                            ) {
                                                Box(Modifier.padding(12.dp)) {
                                                    Text(it)
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }

                        1 -> {
                            AppsList(
                                modifier = Modifier.weight(1f, false),
                                mainViewModel, openApp = {
                                    openApp(it)
                                }, onAppDropdown = { app, type ->
                                    onAppDropdown(app, type)
                                })
                        }

                        2 -> {
                            // Extensions
                            val extensions = uiState.extensionMap.values.toList()
                            val appMap = uiState.originalApps.associateBy { it.packageName }
                            val groupExtensions = groupExtensionsByRequired(extensions)
                            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                                groupExtensions.forEach { group ->
                                    stickyHeader {
                                        Row(
                                            modifier = Modifier
                                                .background(
                                                    MaterialTheme.colorScheme.surface
                                                )
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (appMap.containsKey(group.packageName)) {
                                                appMap[group.packageName]?.let { app ->
                                                    AsyncAppIcon(
                                                        packageName = app.packageName,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                    Spacing(1)
                                                    Text(
                                                        app.label,
                                                        modifier = Modifier.weight(1.0f)
                                                    )
                                                }
                                            } else {
                                                ExtensionIcon(iconSize = 48.dp)
                                                Spacing(1)
                                                Text("Standalone", modifier = Modifier.weight(1.0f))
                                            }
                                        }
                                    }
                                    itemsIndexed(group.extensions) { index, extension ->
                                        Column(Modifier.padding(8.dp)) {
                                            Text(
                                                extension.name,
                                                color = MaterialTheme.colorScheme.primary,
                                            )
                                            extension.description?.let { desc ->
                                                if (desc != extension.name) {
                                                    Spacing(0.5f)
                                                    Text(
                                                        desc,
                                                        color = MaterialTheme.colorScheme.secondary
                                                    )
                                                }
                                            }
                                        }
                                        if (index == group.extensions.size - 1) {
                                            Spacing(1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            BottomSearchBar(mainViewModel, settingsViewModel, openApp = {
                openApp(it)
            }, onAppDropdown = { app, type ->
                onAppDropdown(app, type)
            })
        }
        AppStatsModal(mainViewModel)
        StarItemDialog(mainViewModel)
    } else {
        AlertDialog(title = {
            Text(text = stringResource(id = R.string.missing_permission))
        }, text = {
            Text(text = stringResource(id = R.string.request_permission))
        }, onDismissRequest = {
            mainViewModel.loadApps(context)
        }, confirmButton = {
            TextButton(onClick = {
                ActivityHelper.toDetail(context, context.packageName)
            }) {
                Text(stringResource(id = R.string.go_grant))
            }
        }, dismissButton = {
            TextButton(onClick = {
                mainViewModel.loadApps(context)
            }) {
                Text(stringResource(id = R.string.granted))
            }
        })
    }
}

data class ExtensionGroup(
    val packageName: String,
    val extensions: List<Extension>
)

fun groupExtensionsByRequired(extensions: List<Extension>): List<ExtensionGroup> {
    // Group the extensions by their `required` field or use "@unknown@" if `required` is null or empty
    val groupedByPackageName = extensions.groupBy {
        it.required?.firstOrNull() ?: "@unknown@"
    }

    val standaloneExtensions = mutableListOf<Extension>()

    // Map each group to an ExtensionGroup instance
    return groupedByPackageName.map { (packageName, extensions) ->
        ExtensionGroup(packageName, extensions)
    }
}