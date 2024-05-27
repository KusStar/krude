package com.kuss.krude.interfaces

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

object ExtensionType {
    const val INTENT = "intent"
    const val ACTION = "action"
    const val SCHEME = "scheme"
}

data class Extension(
    val id: String,
    var name: String,
    var description: String? = null,
    var type: String,
    var required: List<String>? = null,
    var keywords: List<String>? = null,
    var uri: String? = null,
    var data: IntentData? = null,
    var priority: Int = 0,
    var filterTarget: String? = null,
    val i18n: I18N? = null
)

data class AppExtensionGroup(
    val name: String,
    val version: String,
    val description: String? = null,
    val main: List<Extension>,
) {
    constructor(single: AppExtensionSingle) : this(name = single.name, version = single.version, description = single.description, listOf(single.main))
}

data class AppExtensionSingle(
    val name: String,
    val version: String,
    val description: String? = null,
    val main: Extension,
)

data class IntentData(
    @SerializedName("package")
    val packageField: String? = null,
    @SerializedName("class")
    val classField: String? = null,
    val extra: JsonObject? = null,
    val flags: Long? = null,
    val action: String? = null,
)


data class I18NExtension(
    var name: String? = null,
    var description: String? = null,
    var keywords: List<String>? = null,
    var type: String? = null,
    var uri: String? = null,
    var data: IntentData? = null,
)

data class I18N(
    val zh: I18NExtension? = null,
    val en: I18NExtension? = null
)
