package com.kuss.krude.ui.components.internal.files

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.ui.components.search.CloseBtn
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.ToastUtils
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

val WAIT_TIME: Long = 1000 / 30

@Composable
fun FilesExtension(
    onBack: () -> Unit,
    focusRequester: FocusRequester,
    data: Extension,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val files = remember { mutableStateListOf<File>() }
    val filteredList = remember { mutableStateListOf<File>() }
    var loadFilesJob by remember { mutableStateOf<Job?>(null) }

    var search by remember {
        mutableStateOf("")
    }

    val pathNavigator = rememberPathNavigator()
    val searchPath =
        remember(pathNavigator.currentPath) { pathNavigator.currentPath.ifEmpty { FileHelper.ROOT_PATH } }

    val listState = rememberLazyListState()

    val openedTabs = remember { mutableStateListOf(FileHelper.ROOT_PATH) }
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var showDeleteAlertDialog by remember { mutableStateOf(false) }
    var toDeleteFile by remember { mutableStateOf<File?>(null) }

    var needScrollBack by remember {
        mutableStateOf(false)
    }
    val prevScroll = remember {
        mutableStateListOf<Int>()
    }

    LaunchedEffect(key1 = needScrollBack) {
        if (needScrollBack) {
            scope.launch {
                delay(WAIT_TIME)
                if (prevScroll.size > 0) {
                    listState.scrollToItem(prevScroll.removeLast())
                    needScrollBack = false
                }
            }
        }
    }

    fun updateCurrentTab() {
        openedTabs[selectedTabIndex] = pathNavigator.currentPath
    }

    fun newTab(path: String, jump: Boolean, jumpedCallback: (() -> Unit)? = null) {
        openedTabs.add(path)
        if (jump) {
            // stupid workaround for IndexOutOfBoundsException
            scope.launch {
                delay(WAIT_TIME)
                selectedTabIndex = openedTabs.lastIndex
                jumpedCallback?.invoke()
            }
        }
    }

    fun goToPath(path: String) {
        search = ""
        pathNavigator.goTo(path)
        updateCurrentTab()
    }

    fun onFileItemClick(file: File) {
        prevScroll.add(listState.firstVisibleItemIndex)
        needScrollBack = false
        if (file.isFile) {
            Timber.d("File: $file")
        } else {
            goToPath(file.absolutePath)
        }
    }

    fun goBack() {
        search = ""
        pathNavigator.goBack()
        updateCurrentTab()

        needScrollBack = true
    }

    fun goForward() {
        search = ""
        pathNavigator.goForward()
        updateCurrentTab()
    }

    fun loadFiles() {
        loadFilesJob?.cancel()
        loadFilesJob = scope.launch {
            withContext(IO) {
                files.clear()
                val list = File(searchPath).listFiles()?.toList() ?: emptyList()
                if (list.isEmpty()) {
                    return@withContext
                }
                files.addAll(list.sortedBy { FilterHelper.getAbbr(it.name.lowercase()) })
                Timber.d("Path: $searchPath, Files: $files")
            }
        }
    }

    fun reload() {
        search = ""
        loadFiles()
    }

    fun closeTab(index: Int) {
        openedTabs.removeAt(index)
        if (selectedTabIndex > 0) {
            selectedTabIndex--
            goToPath(openedTabs[selectedTabIndex])
        }
    }

    BackHandler(enabled = searchPath != FileHelper.ROOT_PATH || selectedTabIndex != 0) {
        if (selectedTabIndex != 0 && searchPath == FileHelper.ROOT_PATH) {
            closeTab(selectedTabIndex)
        } else {
            goBack()
        }
    }

    LaunchedEffect(Unit) {
        if (SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                ActivityHelper.startIntentWithTransition(context, intent)
            }
        }
    }

    LaunchedEffect(searchPath) {
        loadFiles()
    }

    LaunchedEffect(search) {
        if (search.isEmpty()) {
            return@LaunchedEffect
        }
        val filtered = files.filter {
            val nameContains = it.name.contains(
                search, ignoreCase = true
            )
            val pinyinContains =
                it.name.isNotEmpty() && FilterHelper.toPinyinWithAbbr(it.name).contains(
                    search, ignoreCase = true
                )
            nameContains || pinyinContains
        }
        val beforeSet = files.toSet()
        val afterSet = filtered.toSet()
        if (beforeSet != afterSet) {
            filteredList.clear()
            filteredList.addAll(filtered)
        }
    }

    LaunchedEffect(selectedTabIndex) {
        prevScroll.clear()
        needScrollBack = false
    }

    Column {
        val listData = remember(search) {
            if (search.isNotEmpty()) {
                filteredList
            } else {
                files
            }
        }
        TopTab(
            onBack = onBack,
            selectedTabIndex = selectedTabIndex,
            changeTab = { index ->
                selectedTabIndex = index
            },
            openedTabs = openedTabs,
            goToPath = { path ->
                goToPath(path)
            },
            newTab = { path ->
                newTab(path, true)
                goToPath(pathNavigator.currentPath)
            },
            closeTab = { index ->
                closeTab(index)
            },
            pathNavigator = pathNavigator,
            closeAllTabs = {
                openedTabs.clear()
                openedTabs.add(FileHelper.ROOT_PATH)
                selectedTabIndex = 0
                goToPath(FileHelper.ROOT_PATH)
            }
        )
        HorizontalDivider()
        AnimatedContent(
            search.isNotEmpty() && listData.isEmpty(),
            label = "file list",
            modifier = Modifier.weight(1f)
        ) { isEmpty ->
            if (isEmpty) {
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "(˚Δ˚)",
                        fontSize = 96.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "No files found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                val showGoToPreviousDir = remember(pathNavigator.currentPath) {
                    pathNavigator.currentPath != FileHelper.PATH_PREFIX && pathNavigator.currentPath != ""
                }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    state = listState,
                ) {
                    if (showGoToPreviousDir) {
                        item {
                            FileItem(file = File(".."), onClick = {
                                val temp = File(pathNavigator.currentPath)
                                goToPath(temp.parent ?: temp.absolutePath)
                            })
                        }
                    }
                    items(listData) { file ->
                        val targetTabs by remember {
                            derivedStateOf {
                                openedTabs.filterIndexed { index, _ ->
                                    index != selectedTabIndex
                                }
                            }
                        }
                        FileItem(
                            modifier = Modifier, file = file, onClick = {
                                onFileItemClick(file)
                            },
                            openedTabs = targetTabs
                        ) { type, arg ->
                            when (type) {
                                FileDropdownType.OPEN_WITH -> {
                                    // TODO: Open with
                                    val intent =
                                        Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                            // Optionally, specify a starting directory.
                                            // For example, to open the Downloads folder, you might use "Downloads/" as the URI.
                                            // Note: Not all URIs will work on all devices because the path structure can vary.
                                        }
                                    ActivityHelper.startIntentWithTransition(context, intent)
                                }

                                FileDropdownType.OPEN_IN_NEW_TAB -> {
                                    newTab(file.absolutePath, false)
                                }

                                FileDropdownType.JUMP_IN_NEW_TAB -> {
                                    newTab(file.absolutePath, true) {
                                        goToPath(file.absolutePath)
                                    }
                                }

                                FileDropdownType.DELETE -> {
                                    showDeleteAlertDialog = true
                                    toDeleteFile = file
                                }

                                FileDropdownType.COPY_TO -> {
                                    val destDir = arg ?: ""
                                    scope.launch {
                                        withContext(IO) {
                                            FileHelper.copyFileTo(file, destDir)
                                            ToastUtils.show(
                                                context,
                                                "Copied ${file.name} to ${
                                                    FileHelper.formatPath(
                                                        destDir
                                                    )
                                                }"
                                            )
                                        }
                                    }
                                }

                                FileDropdownType.MOVE_TO -> {
                                    val destDir = arg ?: ""
                                    scope.launch {
                                        withContext(IO) {
                                            FileHelper.moveFileTo(file, destDir)
                                            reload()
                                            ToastUtils.show(
                                                context,
                                                "Moved ${file.name} to ${
                                                    FileHelper.formatPath(
                                                        destDir
                                                    )
                                                }"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        HorizontalDivider()
        Breadcrumbs(searchPath, onPath = { nextPath ->
            goToPath(nextPath)
        }, leftContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(pathNavigator.canGoBack) {
                    IconButton(onClick = {
                        goBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "back",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
                AnimatedVisibility(pathNavigator.canForward) {
                    IconButton(onClick = {
                        goForward()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowForward,
                            contentDescription = "forward",
                            modifier = Modifier.size(ButtonDefaults.IconSize),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        })
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            CloseBtn(search.isNotEmpty()) {
                search = ""
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .weight(1f),
                value = search,
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
                    focusedPlaceholderColor = MaterialTheme.colorScheme.secondary,
                ),
                onValueChange = {
                    search = it
                },
                placeholder = {
                    Text(
                        text = data.description ?: "Search anything"
                    )
                },
            )
        }
        Spacer(modifier = Modifier.imePadding())
    }

    if (showDeleteAlertDialog && toDeleteFile != null) {
        val file = toDeleteFile!!
        AlertDialog(
            onDismissRequest = {
                showDeleteAlertDialog = false
            },
            title = {
                Text(text = "Delete ${file.name}?")
            },
            text = {
                Text(text = "Are you sure you want to delete ${file.name}?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAlertDialog = false
                        scope.launch {
                            withContext(IO) {
                                FileHelper.deleteFile(file)
                                reload()
                                ToastUtils.show(context, "Deleted ${file.name}")
                                toDeleteFile = null
                            }
                        }
                    }
                ) {
                    Text(text = "Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteAlertDialog = false
                    }
                ) {
                    Text(text = "Cancel")
                }
            })
    }
}