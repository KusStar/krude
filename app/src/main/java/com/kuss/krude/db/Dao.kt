package com.kuss.krude.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction


@Dao
interface AppDao {
    @Query("SELECT * FROM apps")
    fun getAllApps(): List<AppInfo>

    @Transaction
    @Query("SELECT * FROM apps")
    fun getAppsWithUsages(): List<AppInfoWithUsage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertApp(app: AppInfo)

    @Query("DELETE FROM apps")
    fun deleteAllApp()

    @Query("DELETE FROM apps WHERE packageName = :packageName")
    fun deleteApp(packageName: String)

    @Delete
    fun deleteApp(app: AppInfo)
}


@Dao
interface UsageDao {
    @Query("SELECT * FROM usage where packageName = :packageName")
    fun getPackageUsage(packageName: String): List<Usage>

    @Query("SELECT strftime('%Y-%m-%d', datetime(createdAt/1000, 'unixepoch', 'localtime')) AS day, COUNT(*) as count FROM usage GROUP BY day ORDER BY day DESC")
    fun getUsageCountByDay(): List<UsageCountByDay>

    @Query("SELECT apps.*\n" +
            "        FROM usage\n" +
            "        INNER JOIN apps ON apps.packageName = usage.packageName\n" +
            "        WHERE strftime('%Y-%m-%d', usage.createdAt / 1000, 'unixepoch', 'localtime') = :day")
    fun getAppsByDay(day: String): List<AppInfo>

    @Insert
    fun insertUsage(usage: Usage)

    @Query("DELETE FROM usage")
    fun deleteAllUsage()

    @Delete
    fun deleteUsage(usage: Usage)
}

@Dao
interface StarDao {
    @Query("SELECT * FROM star ORDER BY createdAt DESC")
    fun getAllStars(): List<Star>


    @Query("SELECT * FROM star where keyword = :keyword")
    fun getKeywordStars(keyword: String): List<Star>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertStar(star: Star)

    @Query("DELETE FROM star")
    fun deleteAllStar()

    @Query("DELETE FROM star where key = :key")
    fun deleteStar(key: String)

    @Query("DELETE FROM star where key = :key and keyword = :keyword")
    fun deleteStarPackage(key: String, keyword: String)
}

@Dao
interface HiddenDao {
    @Query("SELECT * FROM hidden ORDER BY createdAt DESC")
    fun getAll(): List<Hidden>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(hidden: Hidden)

    @Query("DELETE FROM hidden")
    fun deleteAll()

    @Query("DELETE FROM hidden where `key` = :key")
    fun delete(key: String)
}

@Dao
interface ExtensionCacheDao {
    @Query("SELECT * FROM extension_cache")
    fun getAll(): List<ExtensionCache>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(extensionCache: ExtensionCache)

    @Query("DELETE FROM extension_cache")
    fun deleteAll()

    @Query("DELETE FROM extension_cache where `id` = :id")
    fun delete(id: String)
}