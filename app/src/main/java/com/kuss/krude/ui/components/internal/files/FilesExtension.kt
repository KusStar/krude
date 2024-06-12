package com.kuss.krude.ui.components.internal.files

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.ui.components.search.CloseBtn
import com.kuss.krude.utils.FilterHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File


val ROOT_PATH: String = Environment.getExternalStoragePublicDirectory("").absolutePath

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FilesExtension(
    onChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    data: Extension,
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val files = remember { mutableStateListOf<File>() }
    val filteredList = remember { mutableStateListOf<File>() }
    var job by remember { mutableStateOf<Job?>(null) }

    var text by remember {
        mutableStateOf("")
    }

    val pathNavigator = rememberPathNavigator(ROOT_PATH)
    var searchPath = pathNavigator.currentPath

    val listState = rememberLazyListState()

    fun onClick(file: File) {
        if (file.isFile) {
            Timber.d("File: $file")
        } else {
            text = ""
            searchPath = file.absolutePath
            pathNavigator.goTo(file.absolutePath)
        }
    }

    fun goBack() {
        text = ""
        if (searchPath == ROOT_PATH) {
            onChange(false)
        } else {
            searchPath = pathNavigator.goBack()
        }
    }

    fun goForward() {
        text = ""
        searchPath = pathNavigator.goForward()
    }

    BackHandler(enabled = searchPath != ROOT_PATH) {
        goBack()
    }

    LaunchedEffect(Unit) {
        if (SDK_INT >= 30) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent()
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                context.startActivity(intent);
            }
        }
    }

    LaunchedEffect(searchPath) {
        job?.cancel()
        job = scope.launch {
            withContext(IO) {
                files.clear()
                val list = File(searchPath).listFiles()?.toList() ?: emptyList()
                if (list.isEmpty()) {
                    return@withContext
                }
                files.addAll(list)
                Timber.d("Path: $searchPath, Files: $files")
            }
        }
    }

    LaunchedEffect(text) {
        val filtered = files.filter {
            val nameContains = it.name.contains(
                text, ignoreCase = true
            )
            val pinyinContains =
                it.name.isNotEmpty() && FilterHelper.toPinyinWithAbbr(it.name).contains(
                    text, ignoreCase = true
                )
            nameContains || pinyinContains
        }.sortedBy { FilterHelper.toAbbr(it.name) }
        val beforeSet = files.toSet()
        val afterSet = filtered.toSet()
        if (beforeSet != afterSet) {
            filteredList.clear()
            filteredList.addAll(filtered)
        }
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Column {
            val listData = if (text.isNotEmpty()) {
                filteredList
            } else {
                files
            }
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .animateContentSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                state = listState
            ) {
                stickyHeader {
                    Breadcrumbs(searchPath, onPath = {
                        text = ""
                        searchPath = it
                    }, leftContent = {
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
                        if (pathNavigator.canForward) {
                            IconButton(onClick = {
                                goForward()
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Default.ArrowForward,
                                    contentDescription = "back",
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    })
                }
                item {
                    FileItem(file = File(".."), onClick = {
                        goBack()
                    })
                }
                items(listData) { file ->
                    FileItem(file = file, onClick = {
                        onClick(file)
                    })
                }
            }
        }
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
            CloseBtn(text.isNotEmpty()) {
                text = ""
            }
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .weight(1f),
                value = text,
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
                    text = it
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
}