package com.kuss.krude.ui.components.internal

import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.I18N
import com.kuss.krude.interfaces.I18NExtension

object InternalExtensions {
    const val FILES_EXTENSION_ID = "internal-files"
    // const val SCANNER_EXTENSION_ID = "internal-scanner"
    const val KILL_EXTENSION_ID = "internal-kill"

    val IDS = setOf(FILES_EXTENSION_ID, KILL_EXTENSION_ID)

    private val FILES_EXTENSION = Extension(
        FILES_EXTENSION_ID,
        "Files",
        "Search Files",
        "internal",
        i18n = I18N(zh = I18NExtension("文件", "搜索文件")),
        keywords = listOf("files", "search", "file manager")
    )

    private val SCANNER_EXTENSION = Extension(
        SCANNER_EXTENSION_ID,
        "Scanner",
        "QRCode Scanner",
        "internal",
        i18n = I18N(zh = I18NExtension("扫一扫", "扫一扫二维码、物体")),
        keywords = listOf("qrcode", "scanner")
    )

    private val KILL_EXTENSION = Extension(
        KILL_EXTENSION_ID,
        "Kill Process",
        "Kill Process",
        "internal",
        keywords = listOf("kill", "process", "close app")
    )

    val ALL = listOf(
        FILES_EXTENSION,
        SCANNER_EXTENSION,
        KILL_EXTENSION
    )
}