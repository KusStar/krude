package com.kuss.krude.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "apps"
)
data class AppInfo(
    @PrimaryKey val packageName: String,
    val label: String,
    val abbr: String,
    val filterTarget: String,
    var priority: Int = 0
)