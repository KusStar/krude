package com.kuss.krude.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.kuss.krude.interfaces.Extension
import java.util.Date


class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun extensionToJson(extension: Extension): String {
        return Gson().toJson(extension)
    }

    @TypeConverter
    fun jsonToExtension(json: String): Extension {
        return Gson().fromJson(json, Extension::class.java)
    }
}

@TypeConverters(Converters::class)
@Database(
    entities = [AppInfo::class, Usage::class, Star::class, Hidden::class, ExtensionCache::class],
    version = 1,
    exportSchema = true,
//    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    abstract fun usageDao(): UsageDao

    abstract fun starDao(): StarDao

    abstract fun hiddenDao(): HiddenDao

    abstract fun extensionCacheDao(): ExtensionCacheDao
}