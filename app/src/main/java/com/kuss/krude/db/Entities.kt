package com.kuss.krude.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.Date

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

@Entity(
    tableName = "usage"
)
data class Usage(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val packageName: String,
    val createdAt: Date = Date()
)


data class UsageCountByDay(
    val day: String,
    val count: Int
)


data class AppInfoWithUsage(
    @Embedded val appInfo: AppInfo,
    @Relation(
        parentColumn = "packageName",
        entityColumn = "packageName"
    )
    val usages: List<Usage>
)