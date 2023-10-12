package com.kuss.krude.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.kuss.krude.data.AppInfo
import com.kuss.krude.data.Usage
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
@Database(entities = [AppInfo::class, Usage::class], version = 1, exportSchema = true, autoMigrations = [])
abstract class AppDatabase : RoomDatabase() {
//    @DeleteColumn(
//        tableName = "apps",
//        columnName = "id"
//    )
//    class Migrate1to2Spec: AutoMigrationSpec {
//        override fun onPostMigrate(db: SupportSQLiteDatabase) {
//            // Callback for any tasks to execute after migration is done
//        }
//    }
    abstract fun appDao(): AppDao

    abstract fun usageDao(): UsageDao

}