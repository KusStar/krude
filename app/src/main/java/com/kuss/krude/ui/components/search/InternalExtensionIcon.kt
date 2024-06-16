package com.kuss.krude.ui.components.search

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.kuss.krude.extensions.InternalExtensions
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.utils.SizeConst

@Composable
fun InternalExtensionIcon(extension: Extension, size: Dp = SizeConst.SEARCH_RESULT_SMALL_ICON_SIZE) {
    if (extension.id == InternalExtensions.FILES_EXTENSION_ID) {
        Icon(
            Icons.Default.Folder,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "Files",
            modifier = Modifier.size(size)
        )
    }
}
