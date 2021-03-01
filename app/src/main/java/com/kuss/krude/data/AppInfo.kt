package com.kuss.krude.data

import android.graphics.Bitmap

data class AppInfo(
    val label: String,
    val packageName: String,
    val icon: Bitmap,
    val filterTarget: String,
    var priority: Int = 0
)
