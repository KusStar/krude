package com.kuss.krude.ui.components.internal.files

import android.icu.text.DateFormat
import android.webkit.MimeTypeMap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.kuss.krude.ui.components.CustomButton
import me.saket.cascade.CascadeDropdownMenu
import java.io.File
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

fun getFileTypeIcon(file: File): ImageVector {
    val ext = MimeTypeMap.getFileExtensionFromUrl(file.name)
    return when (".$ext") {
        ".png", ".jpg", ".jpeg", ".gif", ".bmp" -> Icons.Default.Image
        ".mp3", ".wav", ".ogg", "midi" -> Icons.Default.AudioFile
        ".mp4", ".rmvb", ".avi", ".flv", ".3gp" -> Icons.Default.VideoFile
        ".jsp", ".html", ".htm", ".js", ".php", ".txt", ".c", ".cpp", ".xml", ".py", ".json", ".log" -> Icons.Default.Source
        ".doc", ".docx", ".xls", ".xlsx" -> Icons.AutoMirrored.Filled.Article
        ".ppt", ".pptx" -> Icons.Default.FilePresent
        ".pdf" -> Icons.Default.PictureAsPdf
        ".jar", ".zip", ".rar", ".gz", "img" -> Icons.Default.Archive
        ".apk" -> Icons.Default.Android
        else -> Icons.Default.FileOpen
    }
}

@Composable
fun FileIcon(file: File) {
    val icon = remember {
        getFileTypeIcon(file)
    }
    Icon(
        imageVector = icon,
        contentDescription = "File Icon",
        tint = MaterialTheme.colorScheme.primary
    )
}

fun formatFileSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val base = 1024.0
    val exponent = (ln(sizeInBytes.toDouble()) / ln(base)).toInt()
    val size = sizeInBytes / base.pow(exponent.toDouble())
    val df = DecimalFormat("#.##")
    return "${df.format(size)} ${units[exponent]}"
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
                formatFileSize(file.length())
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
fun FileItem(modifier: Modifier = Modifier, file: File, onClick: () -> Unit) {
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
            Column(verticalArrangement = Arrangement.Center) {
                val isPreviousDir = file.name == ".."
                Text(text = file.name, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.let {
                    if (isPreviousDir) it.titleLarge else it.bodyMedium
                })
                if (!isPreviousDir) {
                    FileDetail(file)
                }
            }
        }

        CascadeDropdownMenu(
            expanded = isMenuVisible,
            onDismissRequest = { isMenuVisible = false },
            offset = DpOffset(128.dp, 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text("Delete") },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Copy to") },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Move to") },
                onClick = { }
            )
            DropdownMenuItem(
                text = { Text("Horizon") },
                children = {
                    DropdownMenuItem(
                        text = { Text("Zero Dawn") },
                        onClick = { }
                    )
                    DropdownMenuItem(
                        text = { Text("Forbidden West") },
                        onClick = { }
                    )
                }
            )
        }
    }
}