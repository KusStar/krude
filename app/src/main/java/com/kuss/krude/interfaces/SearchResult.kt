package com.kuss.krude.interfaces

import com.kuss.krude.db.AppInfo


enum class SearchResultType(val value: String) {
    APP("app"),
    EXTENSION("extension");

    override fun toString(): String {
        return value
    }
}

data class SearchResultItem(
    val type: SearchResultType,
    val app: AppInfo? = null,
    val other: Any? = null
) {
    fun isApp(): Boolean {
        return type == SearchResultType.APP
    }

    fun isExtension(): Boolean {
        return type == SearchResultType.EXTENSION
    }

    fun asApp(): AppInfo? {
        return app
    }

    fun key(): String {
        return if (isApp()) {
            asApp()!!.packageName
        } else {
            other.toString()
        }
    }
}