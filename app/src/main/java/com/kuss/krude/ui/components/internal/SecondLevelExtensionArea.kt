package com.kuss.krude.ui.components.internal

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import com.kuss.krude.extensions.InternalExtensions
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.ui.components.internal.files.FilesExtension

@Composable
fun SecondLevelExtensionArea(
    onChange: (Boolean) -> Unit,
    focusRequester: FocusRequester,
    data: Extension? = null,
) {
    if (data != null) {
        if (data.id == InternalExtensions.FILES_EXTENSION_ID) {
            HorizontalDivider()
            FilesExtension(onChange, focusRequester, data)
        }
    }
}
