package com.kuss.krude.interfaces

import com.kuss.krude.db.AppInfo



enum class SearchResultType(val value: String) {
    APP("app"),
    EXTENSION("extension");

    override fun toString(): String {
        return value
    }
}

class SearchResultItem(
    private val type: SearchResultType,
    private val app: AppInfo? = null,
    private val extension: Extension? = null
) {
    constructor(app: AppInfo) : this(SearchResultType.APP, app = app)
    constructor(extension: Extension) : this(SearchResultType.EXTENSION, extension = extension)

    fun getPriority(): Int {
        return if (isApp()) {
            asApp()!!.priority
        } else {
            extension!!.priority
        }
    }

    fun isApp(): Boolean {
        return type == SearchResultType.APP
    }

    fun isExtension(): Boolean {
        return type == SearchResultType.EXTENSION
    }

    fun asApp(): AppInfo? {
        return app
    }

    fun asExtension(): Extension? {
        return extension
    }

    fun key(): String {
        return if (isApp()) {
            asApp()!!.packageName
        } else {
            extension!!.id
        }
    }
}