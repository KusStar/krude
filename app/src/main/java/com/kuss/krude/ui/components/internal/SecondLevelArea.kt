package com.kuss.krude.ui.components.internal

import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.focus.FocusRequester
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.ui.components.internal.files.FilesExtension
import com.kuss.krude.ui.components.internal.kill.KillExtension
import com.kuss.krude.ui.components.internal.scanner.ScannerExtension

@Composable
fun SecondLevelArea(
    onBack: () -> Unit,
    focusRequester: FocusRequester,
    data: Extension? = null,
) {
    if (data != null) {
        if (data.id == InternalExtensions.FILES_EXTENSION_ID) {
            HorizontalDivider()
            FilesExtension(onBack = onBack, focusRequester, data)
        }
        if (data.id == InternalExtensions.SCANNER_EXTENSION_ID) {
            HorizontalDivider()
            ScannerExtension(focusRequester)
        }
        if (data.id == InternalExtensions.KILL_EXTENSION_ID) {
            HorizontalDivider()
            KillExtension(focusRequester)
        }
    }
}
