package com.kuss.krude.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.kuss.krude.data.AppInfo
import com.kuss.krude.data.Usage
import com.kuss.krude.data.UsageCountByDay
import com.kuss.krude.db.AppDatabase
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.TAG
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch


data class MainState(
    val apps: List<AppInfo> = listOf(),
    val filteredApps: List<AppInfo> = listOf(),
    val scrollbarItems: List<String> = listOf(),
    val currentScrollbarIndex: Int = 0,
    val filtering: String = "",
    val showAppDetailSheet: Boolean = false,
    val selectedDetailApp: AppInfo? = null,
    val showAppUsageSheet: Boolean = false,
)

class MainViewModel : ViewModel() {

    companion object {
        @Volatile
        private var DB: AppDatabase? = null

        @Volatile
        private var packageEventReceiver: BroadcastReceiver? = null

        fun getDatabase(context: Context): AppDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return DB ?: synchronized(this) {
                Room.databaseBuilder(context, AppDatabase::class.java, "krude_db")
                    .build()
                    .also { DB = it }
            }
        }
    }

    private val _state = MutableStateFlow(MainState())

    fun initPackageEventReceiver(context: Context) {
        if (packageEventReceiver == null) {
            packageEventReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Log.d(TAG, "onReceive: ${intent.action}")
                    when (intent.action) {
                        Intent.ACTION_PACKAGE_ADDED -> {
                            onPackageAdded(context, intent)
                        }

                        Intent.ACTION_PACKAGE_REMOVED -> {
                            onPackageRemoved(intent)
                        }
                    }
                }
            }

            val filter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            filter.addDataScheme("package")

            context.registerReceiver(packageEventReceiver, filter)
        }
    }

    val state: StateFlow<MainState>
        get() = _state

    fun onPackageAdded(context: Context, intent: Intent) {
        val intentPackageName = intent.dataString?.substring(8)
            ?: return

        val list = ActivityHelper
            .findActivitiesForPackage(context, intentPackageName)
            ?: return

        val pm = context.packageManager

        val apps = state.value.apps.toMutableList()

        for (item in list) {
            if (item == null) continue
            try {
                val app = AppHelper.getAppInfo(
                    item.activityInfo.applicationInfo,
                    pm,
                    context
                )
                Log.i(TAG, "onPackageAdded: ${app.label}, ${app.packageName}")
                apps.add(
                    app
                )
            } catch (e: Exception) {
                e.printStackTrace()
                continue
            }
        }

        FilterHelper.getSorted(apps).let { sorted ->
            _state.update { mainState ->
                mainState.copy(apps = sorted, showAppDetailSheet = false, filtering = "")
            }
        }
    }

    fun onPackageRemoved(intent: Intent) {
        val apps = state.value.apps.toMutableList()
        val toDeletePackageName = intent.dataString?.substring(8)
            ?: return

        val removedIndex = apps.indexOfFirst { it.packageName == toDeletePackageName }
        if (removedIndex != -1) {
            Log.i(TAG, "onPackageRemoved: removedIndex: $removedIndex, ${apps[removedIndex].label}")

            apps.removeAt(removedIndex)

            _state.update { mainState ->
                mainState.copy(apps = apps, showAppDetailSheet = false, filtering = "")
            }
        }
    }

    fun loadApps(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                // load from db
                val db = getDatabase(context)

                val dbApps = db.appDao().getAllApps()

                if (dbApps.isNotEmpty()) {
                    _state.update { mainState ->
                        mainState.copy(
                            apps = dbApps,
                            scrollbarItems = getScrollbarItemsFromApps(dbApps)
                        )
                    }
                    Log.d(TAG, "load from db, ${dbApps.size} apps")
                }
                // load from packageManager
                loadFromPackageManger(context, dbApps)
            }
        }

    }

    fun getUsageCountByDay(context: Context): List<UsageCountByDay> {
        val db = getDatabase(context)

        return db.usageDao().getUsageCountByDay()
    }

    fun getAppsByDay(context: Context, day: String): List<AppInfo> {
        val db = getDatabase(context)

        return db.usageDao().getAppsByDay(day)
    }

    fun loadFromPackageManger(context: Context, dbApps: List<AppInfo>? = null) {
        viewModelScope.launch {
            withContext(IO) {
                val apps = AppHelper.getInstalled(context)

                _state.update { mainState ->
                    mainState.copy(
                        apps = apps,
                        scrollbarItems = getScrollbarItemsFromApps(apps)
                    )
                }

                updateDbAppsPriority(context, apps)

                Log.d(TAG, "load from packageManager, ${apps.size} apps")

                checkAndCleanDbApps(context, apps, dbApps)
            }
        }
    }

    private fun checkAndCleanDbApps(context: Context, apps: List<AppInfo>, dbApps: List<AppInfo>?) {
        viewModelScope.launch {
            withContext(IO) {
                if (dbApps?.size!! > apps.size) {
                    val appsSet = apps.map{it.packageName}.toSet()
                    val db = getDatabase(context)

                    dbApps.forEach {
                        if (!appsSet.contains(it.packageName)) {
                            db.appDao().deleteApp(it)
                        }
                    }
                }
            }

        }
    }


    private fun updateDbAppsPriority(context: Context, apps: List<AppInfo>) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val priorityMap = HashMap<String, Int>()

                db.appDao().getAllApps().forEach {
                    priorityMap[it.packageName] = it.priority
                }

                apps.forEach {
                    it.priority = priorityMap[it.packageName] ?: 0
                    db.appDao().insertApp(
                        it
                    )
                }
            }
        }
    }

    fun resetDbAppsPriority(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val apps = state.value.apps
                apps.forEach {
                    it.priority = 0
                    db.appDao().insertApp(
                        it
                    )
                }
            }
        }
    }

    fun recordOpenApp(context: Context, app: AppInfo) {
        viewModelScope.launch {
            withContext(IO) {
                val apps = state.value.apps.toMutableList()
                val idx = apps.indexOf(app)

                if (idx >= 0) {
                    var item = apps[idx]
                    item = item.copy(priority = item.priority + 1)

                    apps[idx] = item

                    _state.update { mainState ->
                        mainState.copy(
                            apps = apps
                        )
                    }

                    val db = getDatabase(context)

                    db.usageDao().insertUsage(Usage(packageName = app.packageName))

                    db.appDao().insertApp(item)
                }
            }
        }
    }


    fun setFiltering(filtering: String) {
        _state.update { mainState ->
            mainState.copy(filtering = filtering)
        }
    }

    fun setShowAppDetailSheet(visible: Boolean) {
        _state.update { mainState ->
            mainState.copy(
                showAppDetailSheet = visible
            )
        }
    }

    fun setShowAppUsageSheet(visible: Boolean) {
        _state.update { mainState ->
            mainState.copy(
                showAppUsageSheet = visible
            )
        }
    }

    fun setSelectedDetailApp(app: AppInfo) {
        _state.update { mainState ->
            mainState.copy(
                selectedDetailApp = app
            )
        }
    }

    fun setSelectedHeaderIndex(i: Int) {
        _state.update { mainState ->
            mainState.copy(
                currentScrollbarIndex = i
            )
        }
    }

    fun filterApps(apps: List<AppInfo>, text: String, fuzzy: Boolean) {
        viewModelScope.launch {
            val search = text.lowercase()
            var match = apps.filter {
                it.abbr.lowercase().contains(search) || it.filterTarget.lowercase()
                    .contains(search)
            }.sortedByDescending { it.priority }
            if (fuzzy) {
                match =
                    apps
                        .asSequence()
                        .map {
                            val ratio = FuzzySearch.partialRatio(
                                it.abbr.lowercase() + " " + it.filterTarget.lowercase(),
                                search
                            )
                            Fuzzy(it, ratio)
                        }
                        .filter {
                            (it.ratio >= 50)
                        }
                        .sortedByDescending {
                            it.ratio * (it.app.priority + 1)
                        }
                        .map {
                            it.app
                        }
                        .toList()
            }

            _state.update { mainState ->
                mainState.copy(filteredApps = match, filtering = text)
            }
        }
    }

    private fun getScrollbarItemsFromApps(apps: List<AppInfo>): List<String> {
        return apps.map { it.abbr.first().uppercase() }
            .toSet().toList().sorted()
    }
}

data class Fuzzy(val app: AppInfo, val ratio: Int)