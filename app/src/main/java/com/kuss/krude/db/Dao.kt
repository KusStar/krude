package com.kuss.krude.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kuss.krude.data.AppInfo
import com.kuss.krude.data.AppInfoWithUsage
import com.kuss.krude.data.Usage
import com.kuss.krude.data.UsageCountByDay


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

    @Delete
    fun deleteApp(app: AppInfo)
}

@Dao
interface UsageDao {
    @Query("SELECT * FROM usage where packageName = :packageName")
    fun getPackageUsage(packageName: String): List<Usage>

    @Query("SELECT strftime('%Y-%m-%d', datetime(createdAt/1000, 'unixepoch', 'localtime')) AS day, COUNT(*) as count FROM usage GROUP BY day")
    fun getUsageCountByDay(): List<UsageCountByDay>

    @Insert
    fun insertUsage(usage: Usage)

    @Query("DELETE FROM usage")
    fun deleteAllUsage()

    @Delete
    fun deleteUsage(usage: Usage)
}
