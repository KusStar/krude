package com.kuss.krude.extensions

import com.kuss.krude.interfaces.Extension

object InternalExtensions {
    const val FILES_EXTENSION_ID = "internal-files"
}

val FILES_EXTENSION = Extension(InternalExtensions.FILES_EXTENSION_ID, "Files", "Files Manager", "internal")
