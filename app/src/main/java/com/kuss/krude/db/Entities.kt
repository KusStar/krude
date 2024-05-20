package com.kuss.krude.db

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.kuss.krude.interfaces.Extension
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

@Entity(
    tableName = "star"
)
data class Star(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val keyword: String,
    val packageName: String,
    val createdAt: Date = Date()
)

@Entity(
    tableName = "hidden"
)
data class Hidden(
    @PrimaryKey
    val key: String,
    val createdAt: Date = Date(),
)

@Entity(
    tableName = "extension_cache"
)
data class ExtensionCache(
    @PrimaryKey
    val id: String,
    val extension: Extension
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