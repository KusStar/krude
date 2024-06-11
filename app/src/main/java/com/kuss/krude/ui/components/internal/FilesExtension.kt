package com.kuss.krude.ui.components.internal

import android.content.Intent
import android.os.Build.VERSION.SDK_INT
import android.os.Environment
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.ui.components.CustomButton
import com.kuss.krude.ui.components.search.CloseBtn
import com.kuss.krude.utils.FilterHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

@Composable
fun Breadcrumbs(path: String, onPath: (String) -> Unit) {
    val breadcrumbs = path.split("/").filter { it.isNotEmpty() }
    Surface(color = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for ((index, breadcrumb) in breadcrumbs.withIndex()) {
                TextButton(onClick = {
                    onPath(breadcrumbs.subList(0, index + 1).joinToString("/"))
                }) {
                    Text(
                        text = breadcrumb,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.let {
                            if (index == breadcrumbs.lastIndex) it.primary else it.secondary
                        })
                }
                Text(
                    "/",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
fun FileItem(modifier: Modifier = Modifier, file: File, onClick: () -> Unit) {
    CustomButton(
        onClick = {
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.isFile) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "File Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder Icon",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = file.name, color = MaterialTheme.colorScheme.primary)
        }
    }
}

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
    var searchPath by remember { mutableStateOf(ROOT_PATH) }
    val files = remember { mutableStateListOf<File>() }
    var job by remember { mutableStateOf<Job?>(null) }

    var text by remember {
        mutableStateOf("")
    }

    fun goBack() {
        text = ""
        if (searchPath == ROOT_PATH) {
            onChange(false)
        } else {
            searchPath = File(searchPath).parent ?: ROOT_PATH
        }
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

    LaunchedEffect(text, searchPath) {
        job?.cancel()
        job = scope.launch {
            withContext(IO) {
                val list = File(searchPath).listFiles()?.toList() ?: emptyList()
                if (list.isEmpty()) {
                    files.clear()
                    return@withContext
                }
                val filtered = list.filter {
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
                if (beforeSet == afterSet) return@withContext
                files.clear()
                files.addAll(filtered)
                Timber.d("Path: $searchPath, Files: $files")
            }
        }
    }

    Column {
        Column {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                stickyHeader {
                    Breadcrumbs(searchPath, onPath = {
                        text = ""
                        searchPath = it
                    })
                }
                items(files) { file ->
                    FileItem(file = file, onClick = {
                        if (file.isFile) {
                            Timber.d("File: $file")
                        } else {
                            text = ""
                            searchPath = file.absolutePath
                        }
                    })
                }
                if (files.isEmpty()) {
                    item {
                        Text(
                            text = "No files found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }
        HorizontalDivider()
        Row(verticalAlignment = Alignment.CenterVertically) {
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
    }
}