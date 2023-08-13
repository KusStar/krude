package com.kuss.krude.data

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "apps")
data class AppInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String,
    val abbr: String,
    val packageName: String,
    val filterTarget: String,
    var priority: Int = 0
)