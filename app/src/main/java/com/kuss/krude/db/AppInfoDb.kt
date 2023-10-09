package com.kuss.krude.db

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import com.kuss.krude.data.AppInfo

@Dao
interface AppDao {
    @Query("SELECT * FROM apps")
    fun getAllApps(): List<AppInfo>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApp(app: AppInfo)

    @Query("DELETE FROM apps")
    fun deleteAllApp()

    @Delete
    fun deleteApp(app: AppInfo)
}

@Database(entities = [AppInfo::class], version = 1, exportSchema = true, autoMigrations = [])
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
}