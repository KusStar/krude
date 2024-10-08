package com.kuss.krude.ui.components.internal

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.utils.SizeConst

@Composable
fun InternalExtensionIcon(
    extension: Extension,
    size: Dp = SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE
) {
    if (extension.id == InternalExtensions.FILES_EXTENSION_ID) {
        Icon(
            Icons.Default.Folder,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Files",
            modifier = Modifier.size(size)
        )
    }
    if (extension.id == InternalExtensions.SCANNER_EXTENSION_ID) {
        Icon(
            Icons.Default.QrCodeScanner,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Scanner",
            modifier = Modifier.size(size)
        )
    }
    if (extension.id == InternalExtensions.KILL_EXTENSION_ID) {
        Icon(
            Icons.Default.Memory,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Kill",
            modifier = Modifier.size(size)
        )
    }
}
