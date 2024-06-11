package com.kuss.krude.extensions

import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.I18N
import com.kuss.krude.interfaces.I18NExtension

object InternalExtensions {
    const val FILES_EXTENSION_ID = "internal-files"
}

val FILES_EXTENSION = Extension(
    InternalExtensions.FILES_EXTENSION_ID,
    "Files",
    "Search Files",
    "internal",
    i18n = I18N(zh = I18NExtension("文件", "搜索文件")),
    keywords = listOf("files", "search", "file manager")
)