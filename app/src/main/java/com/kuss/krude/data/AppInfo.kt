package com.kuss.krude.data

import android.graphics.Bitmap

data class AppInfo(
    val label: String,
    val abbr: String,
    val packageName: String,
    val filterTarget: String,
    var priority: Int = 0
)

data class AppInfoWithIcon(
    val label: String,
    val abbr: String,
    val packageName: String,
    val filterTarget: String,
    val icon: Bitmap,
    var priority: Int = 0
)