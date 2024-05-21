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
    val description: String? = null,
    val type: String,
    var required: List<String>? = null,
    val keywords: List<String>? = null,
    val uri: String? = null,
    val data: IntentData? = null,
    var priority: Int = 0,
    var filterTarget: String? = null
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
    val packageField: String,
    @SerializedName("class")
    val classField: String,
    val extra: JsonObject? = null,
    val flags: Long? = null,
    val action: String? = null,
)

