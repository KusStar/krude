package com.kuss.krude.ui.components.internal.files

import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FilePresent
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.ui.graphics.vector.ImageVector
import java.io.File
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.pow

enum class FileDropdownType {
    OPEN_WITH,
    OPEN_IN_NEW_TAB,
    JUMP_IN_NEW_TAB,
    DELETE,
    COPY_TO,
    MOVE_TO,
}

object FileHelper {
    val ROOT_PATH: String = Environment.getExternalStoragePublicDirectory("").absolutePath

    val PATH_SUGGESTIONS = listOf<String>(
        "",
        Environment.DIRECTORY_DOWNLOADS,
        Environment.DIRECTORY_DCIM,
        Environment.DIRECTORY_DOCUMENTS,
        Environment.DIRECTORY_MUSIC,
        Environment.DIRECTORY_PICTURES,
        Environment.DIRECTORY_MOVIES,
    )

    const val PATH_PREFIX = "/storage/emulated/0"

    @JvmStatic
    fun formatPath(path: String): String {
        return "~${path.removePrefix(PATH_PREFIX)}"
    }

    @JvmStatic
    fun formatPath(file: File): String {
        return formatPath(file.absolutePath)
    }

    @JvmStatic
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

    @JvmStatic
    fun formatFileSize(sizeInBytes: Long): String {
        if (sizeInBytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val base = 1024.0
        val exponent = (ln(sizeInBytes.toDouble()) / ln(base)).toInt()
        val size = sizeInBytes / base.pow(exponent.toDouble())
        val df = DecimalFormat("#.##")
        return "${df.format(size)} ${units[exponent]}"
    }

    @JvmStatic
    fun copyFileTo(src: File, destDir: String) {
        val dest = File("$destDir/${src.name}")
        src.inputStream().use { input ->
            dest.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    @JvmStatic
    fun moveFileTo(src: File, destDir: String) {
        val dest = File("$destDir/${src.name}")
        src.renameTo(dest)
    }

    @JvmStatic
    fun deleteFile(file: File) {
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }
}