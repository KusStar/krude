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
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
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
import com.kuss.krude.ui.components.CustomButton
import me.saket.cascade.CascadeDropdownMenu
import java.io.File


@Composable
fun FileIcon(file: File) {
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
fun FileDetail(file: File) {
    val date = remember {
        DateFormat.getDateTimeInstance().format(file.lastModified())
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (file.isFile) {
            val size = remember {
                FileHelper.formatFileSize(file.length())
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
fun FileItem(
    modifier: Modifier = Modifier,
    file: File,
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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(verticalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)) {
                val isPreviousDir = file.name == ".."
                Text(
                    text = file.name,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.let {
                        if (isPreviousDir) it.titleLarge else it.bodyMedium
                    })
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
                if (onDropdown != null) {
                    CascadeDropdownMenu(
                        expanded = isMenuVisible,
                        onDismissRequest = { isMenuVisible = false },
                    ) {
                        DropdownMenuItem(
                            text = { Text("Open in new tab") },
                            onClick = {
                                onDropdown(FileDropdownType.OPEN_IN_NEW_TAB, null)
                                isMenuVisible = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                onDropdown(FileDropdownType.DELETE, null)
                                isMenuVisible = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy to") },
                            children = {
                                for (suggest in openedTabs) {
                                    if (suggest.isEmpty()) continue
                                    DropdownMenuItem(
                                        text = { Text(FileHelper.formatPath(suggest)) },
                                        onClick = {
                                            onDropdown(FileDropdownType.COPY_TO, suggest)
                                            isMenuVisible = false
                                        }
                                    )
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Move to") },
                            children = {
                                for (suggest in openedTabs) {
                                    if (suggest.isEmpty()) continue
                                    DropdownMenuItem(
                                        text = { Text(FileHelper.formatPath(suggest)) },
                                        onClick = {
                                            onDropdown(FileDropdownType.MOVE_TO, suggest)
                                            isMenuVisible = false
                                        }
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}