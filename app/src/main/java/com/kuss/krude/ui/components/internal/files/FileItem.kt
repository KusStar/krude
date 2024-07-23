package com.kuss.krude.ui.components.internal.files

import android.icu.text.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Tab
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.kuss.krude.shizuku.bean.BeanFile
import com.kuss.krude.ui.components.CustomButton
import com.kuss.krude.ui.components.HighlightedText
import me.saket.cascade.CascadeDropdownMenu


@Composable
fun FileIcon(file: BeanFile) {
    val icon = remember {
        FileHelper.getFileTypeIcon(file)
    }
    Icon(
        imageVector = icon,
        contentDescription = "File Icon",
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
fun FileDetail(file: BeanFile) {
    val date = remember {
        DateFormat.getDateTimeInstance().format(file.lastModified)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (file.isFile) {
            val size = remember {
                FileHelper.formatFileSize(file.length)
            }
            VerticalDivider(
                modifier = Modifier
                    .padding(4.dp)
                    .height(8.dp)
            )
            Text(
                text = size,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun FileDropdownMenu(
    visible: Boolean,
    file: BeanFile,
    onDismiss: () -> Unit,
    openedTabs: List<String>,
    onDropdown: ((type: FileDropdownType, arg: String?) -> Unit)? = null
) {
    if (onDropdown != null) {
        val isFile = file.isFile
        CascadeDropdownMenu(
            expanded = visible,
            onDismissRequest = { onDismiss() },
        ) {
//            DropdownMenuItem(
//                text = { Text("Open with") },
//                leadingIcon = {
//                    Icon(
//                        imageVector = Icons.Default.OpenWith,
//                        contentDescription = "Open with Icon",
//                    )
//                },
//                onClick = {
//                    onDropdown(FileDropdownType.OPEN_WITH, null)
//                    onDismiss()
//                }
//            )
            DropdownMenuItem(
                text = { Text("Open in new tab") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Tab,
                        contentDescription = "New Tab Icon",
                    )
                },
                onClick = {
                    onDropdown(FileDropdownType.OPEN_IN_NEW_TAB, null)
                    onDismiss()
                }
            )
            DropdownMenuItem(
                text = { Text("Jump in new tab") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Jump Tab Icon",
                    )
                },
                onClick = {
                    onDropdown(FileDropdownType.JUMP_IN_NEW_TAB, null)
                    onDismiss()
                }
            )
            DropdownMenuItem(
                text = { Text("Delete") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete icon",
                    )
                },
                onClick = {
                    onDropdown(FileDropdownType.DELETE, null)
                    onDismiss()
                })
            if (openedTabs.isNotEmpty()) {
                DropdownMenuItem(
                    text = { Text("Copy to") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isFile) Icons.Default.FileCopy else Icons.Default.FolderCopy,
                            contentDescription = "File Icon",
                        )
                    },
                    children = {
                        for (suggest in openedTabs) {
                            DropdownMenuItem(text = {
                                Text(
                                    FileHelper.formatPath(
                                        suggest.ifEmpty { "~" }
                                    )
                                )
                            }, onClick = {
                                onDropdown(FileDropdownType.COPY_TO, suggest)
                                onDismiss()
                            })
                        }
                    })
                DropdownMenuItem(
                    text = { Text("Move to") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                            contentDescription = "Move Icon",
                        )
                    },
                    children = {
                        for (suggest in openedTabs) {
                            DropdownMenuItem(text = {
                                Text(
                                    FileHelper.formatPath(
                                        suggest.ifEmpty { "~" }
                                    )
                                )
                            }, onClick = {
                                onDropdown(FileDropdownType.MOVE_TO, suggest)
                                onDismiss()
                            })
                        }
                    })
            }
        }
    }
}


@Composable
fun FileItem(
    modifier: Modifier = Modifier,
    highlight: String = "",
    file: BeanFile,
    onClick: () -> Unit,
    openedTabs: List<String> = emptyList(),
    onDropdown: ((type: FileDropdownType, arg: String?) -> Unit)? = null
) {
    var isMenuVisible by rememberSaveable { mutableStateOf(false) }
    CustomButton(
        onClick = {
            onClick()
        },
        onLongClick = {
            isMenuVisible = true
        },
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
        ) {
            if (file.isFile) {
                FileIcon(file)
            } else {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Folder Icon",
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                val isPreviousDir = file.name == ".."
                if (highlight.isNotEmpty()) {
                    HighlightedText(
                        text = file.name,
                        highlightText = highlight,
                        style = MaterialTheme.typography.let {
                            if (isPreviousDir) it.titleLarge else it.bodyMedium
                        })
                } else {
                    Text(
                        text = file.name,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.let {
                            if (isPreviousDir) it.titleLarge else it.bodyMedium
                        })
                }

                if (!isPreviousDir) {
                    FileDetail(file)
                }
            }
            Box {
                IconButton(onClick = { isMenuVisible = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Icon",
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                    )
                }
                FileDropdownMenu(
                    visible = isMenuVisible,
                    file = file,
                    onDismiss = { isMenuVisible = false },
                    openedTabs = openedTabs,
                    onDropdown = onDropdown
                )
            }
        }
    }
}