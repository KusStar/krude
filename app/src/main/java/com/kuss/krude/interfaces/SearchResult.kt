package com.kuss.krude.interfaces

import com.kuss.krude.db.AppInfo

enum class ExtensionType(val value: String) {
    ACTIVITY("activity"),
    ACTION("action"),
    SCHEME("scheme");

    override fun toString(): String {
        return value
    }
}

data class Extension(
    val name: String,
    val description: String,
    val type: ExtensionType,
    val uri: String,
    var priority: Int = 0,
    var filterTarget: String? = null
)

enum class SearchResultType(val value: String) {
    APP("app"),
    EXTENSION("extension");

    override fun toString(): String {
        return value
    }
}

class SearchResultItem(
    val type: SearchResultType,
    val app: AppInfo? = null,
    val extension: Extension? = null
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
            extension!!.name
        }
    }
}