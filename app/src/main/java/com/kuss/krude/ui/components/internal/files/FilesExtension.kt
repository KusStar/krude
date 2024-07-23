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
import androidx.compose.material3.Card
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.shizuku.FileExplorerServiceManager
import com.kuss.krude.shizuku.bean.BeanFile
import com.kuss.krude.ui.components.search.CloseBtn
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.ToastUtils
import com.kuss.krude.shizuku.rememberShizukuState
import com.kuss.krude.viewmodel.extensions.FilesExtensionViewModel
import com.kuss.krude.viewmodel.extensions.FilesOrderBy
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

const val WAIT_TIME: Long = 1000 / 30

@Composable
fun FilesExtension(
    onBack: () -> Unit,
    focusRequester: FocusRequester,
    data: Extension,
    viewModel: FilesExtensionViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val state by viewModel.state.collectAsState()
    val search = state.search
    val pathNavigator = state.pathNavigator
    val currentPath =
        remember(pathNavigator.currentPath) { pathNavigator.currentPath.ifEmpty { FileHelper.ROOT_PATH } }
    val tabs = state.tabs
    val currentTabIndex = state.currentTabIndex

    var showDeleteAlertDialog by remember { mutableStateOf(false) }
    var toDeleteFile by remember { mutableStateOf<BeanFile?>(null) }

    var needScrollBack by remember {
        mutableStateOf(false)
    }
    val prevScroll = remember {
        mutableStateListOf<Int>()
    }

    val shizukuState = rememberShizukuState()

    val filePreviewState = rememberFilePreviewState()

    LaunchedEffect(shizukuState.hasBinder, shizukuState.hasPermission) {
        if (shizukuState.hasBinder && shizukuState.hasPermission) {
            FileExplorerServiceManager.bindService()
        }
    }

    LaunchedEffect(state.filesOrderBy, state.showHiddenFiles) {
        listState.animateScrollToItem(0)
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

    fun onFileItemClick(file: BeanFile) {
        prevScroll.add(listState.firstVisibleItemIndex)
        needScrollBack = false
        if (file.isFile) {
            Timber.d("File: $file")
            filePreviewState.show(file)
        } else {
            viewModel.goToPath(file.absolutePath)
        }
    }

    fun goBack() {
        viewModel.setSearch("")
        pathNavigator.goBack()
        viewModel.updateCurrentTab()

        needScrollBack = true
    }

    fun reload() {
        viewModel.setSearch("")
        viewModel.loadFiles(currentPath)
    }

    BackHandler(enabled = currentPath != FileHelper.ROOT_PATH || currentTabIndex != 0) {
        if (currentTabIndex != 0 && currentPath == FileHelper.ROOT_PATH) {
            viewModel.closeTab(currentTabIndex)
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

    LaunchedEffect(currentPath) {
        viewModel.loadFiles(currentPath)
    }

    LaunchedEffect(search) {
        if (search.isEmpty()) {
            return@LaunchedEffect
        }
        val filtered = state.files.filter {
            val nameContains = it.name.contains(
                search, ignoreCase = true
            )
            val pinyinContains =
                it.name.isNotEmpty() && FilterHelper.toPinyinWithAbbr(it.name).contains(
                    search, ignoreCase = true
                )
            nameContains || pinyinContains
        }
        val beforeSet = state.files.toSet()
        val afterSet = filtered.toSet()
        if (beforeSet != afterSet) {
            viewModel.setFilteredFiles(filtered)
        }
    }

    LaunchedEffect(currentTabIndex) {
        prevScroll.clear()
        needScrollBack = false
    }

    Column {
        val listData = remember(
            search, state.files, state.filteredFiles, state.showHiddenFiles, state.filesOrderBy
        ) {
            val list = if (search.isNotEmpty()) {
                state.filteredFiles
            } else {
                state.files
            }
            val comparator = when (state.filesOrderBy) {
                FilesOrderBy.ALPHABET_ASC -> {
                    compareBy<BeanFile> { FilterHelper.getAbbr(it.name) }
                }

                FilesOrderBy.ALPHABET_DESC -> {
                    compareByDescending { FilterHelper.getAbbr(it.name) }
                }

                FilesOrderBy.DATE_ASC -> {
                    compareBy { it.lastModified }
                }

                FilesOrderBy.DATE_DESC -> {
                    compareByDescending { it.lastModified }
                }

                FilesOrderBy.SIZE_ASC -> {
                    compareBy { it.length }
                }

                FilesOrderBy.SIZE_DESC -> {
                    compareByDescending { it.length }
                }
            }
            return@remember if (!state.showHiddenFiles) {
                list.filter {
                    !it.isHidden
                }
            } else {
                list
            }.sortedWith(comparator)
        }
        TopTab(
            onBack = onBack, viewModel = viewModel
        )
        HorizontalDivider()
        AnimatedContent(
            search.isNotEmpty() && listData.isEmpty(),
            label = "file list",
            modifier = Modifier.weight(1f),
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
                    modifier = Modifier
                        .weight(1f),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    verticalArrangement = Arrangement.Bottom,
                    state = listState
                ) {
                    if (showGoToPreviousDir) {
                        item {
                            FileItem(
                                file = BeanFile(".."), onClick = {
                                    val temp = File(pathNavigator.currentPath)
                                    viewModel.goToPath(temp.parent ?: temp.absolutePath)
                                })
                        }
                    }
                    items(listData, key = { it.path }) { file ->
                        val targetTabs by remember {
                            derivedStateOf {
                                tabs.filterIndexed { index, _ ->
                                    index != currentTabIndex
                                }
                            }
                        }
                        FileItem(
                            highlight = search,
                            modifier = Modifier,
                            file = file,
                            onClick = {
                                onFileItemClick(file)
                            },
                            openedTabs = targetTabs
                        ) { type, arg ->
                            when (type) {
                                FileDropdownType.OPEN_WITH -> {
                                    // TODO: Open with
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                                        // Optionally, specify a starting directory.
                                        // For example, to open the Downloads folder, you might use "Downloads/" as the URI.
                                        // Note: Not all URIs will work on all devices because the path structure can vary.
                                    }
                                    ActivityHelper.startIntentWithTransition(context, intent)
                                }

                                FileDropdownType.OPEN_IN_NEW_TAB -> {
                                    viewModel.newTab(file.absolutePath, false)
                                }

                                FileDropdownType.JUMP_IN_NEW_TAB -> {
                                    viewModel.newTab(file.absolutePath, true) {
                                        viewModel.goToPath(file.absolutePath)
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
                                                context, "Copied ${file.name} to ${
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
                                                context, "Moved ${file.name} to ${
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
        Breadcrumbs(currentPath, onPath = { nextPath ->
            viewModel.goToPath(nextPath)
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
                        viewModel.goForward()
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
                viewModel.setSearch("")
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
                    viewModel.setSearch(it)
                },
                placeholder = {
                    Text(
                        text = data.description ?: "Search anything"
                    )
                },
            )
            if (search.isNotEmpty()) {
                Text(
                    text = "${state.filteredFiles.size}/${state.files.size}",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 16.dp)
                )
            } else {
                Text(
                    text = "${state.files.size}",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.imePadding())
    }

    if (showDeleteAlertDialog && toDeleteFile != null) {
        val file = toDeleteFile!!
        AlertDialog(onDismissRequest = {
            showDeleteAlertDialog = false
        }, title = {
            Text(text = "Delete ${file.name}?")
        }, text = {
            Text(text = "Are you sure you want to delete ${file.name}?")
        }, confirmButton = {
            TextButton(onClick = {
                showDeleteAlertDialog = false
                scope.launch {
                    withContext(IO) {
                        FileHelper.deleteFile(file)
                        reload()
                        ToastUtils.show(context, "Deleted ${file.name}")
                        toDeleteFile = null
                    }
                }
            }) {
                Text(text = "Delete")
            }
        }, dismissButton = {
            TextButton(onClick = {
                showDeleteAlertDialog = false
            }) {
                Text(text = "Cancel")
            }
        })
    }

    if (filePreviewState.previewing) {
        Dialog(onDismissRequest = { filePreviewState.dismiss() }) {
            Card {
                FilePreview(file = File(filePreviewState.file!!.path))
            }
        }
    }
}