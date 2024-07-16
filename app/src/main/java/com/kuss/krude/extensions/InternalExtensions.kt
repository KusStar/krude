package com.kuss.krude.extensions

import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.I18N
import com.kuss.krude.interfaces.I18NExtension

object InternalExtensions {
    const val FILES_EXTENSION_ID = "internal-files"
    const val SCANNER_EXTENSION_ID = "internal-scanner"

    val IDS = setOf(FILES_EXTENSION_ID, SCANNER_EXTENSION_ID)

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

    val ALL = listOf(
        FILES_EXTENSION,
        SCANNER_EXTENSION
    )
}