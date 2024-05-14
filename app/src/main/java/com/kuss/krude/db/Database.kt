package com.kuss.krude.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
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
}

@TypeConverters(Converters::class)
@Database(
    entities = [AppInfo::class, Usage::class, Star::class, Hidden::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2), AutoMigration(from = 2, to = 3)]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    abstract fun usageDao(): UsageDao

    abstract fun starDao(): StarDao

    abstract fun hiddenDao(): HiddenDao
}