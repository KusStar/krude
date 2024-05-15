package com.kuss.krude.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.kuss.krude.db.AppDatabase
import com.kuss.krude.db.AppInfo
import com.kuss.krude.db.Hidden
import com.kuss.krude.db.Star
import com.kuss.krude.db.Usage
import com.kuss.krude.db.UsageCountByDay
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.ExtensionHelper
import com.kuss.krude.utils.FilterHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import timber.log.Timber

data class MainState(
    val missingPermission: Boolean = false,
    val apps: List<AppInfo> = listOf(),
    val searchResult: List<SearchResultItem> = listOf(),
    val scrollbarItems: List<String> = listOf(),
    val currentScrollbarIndex: Int = 0,
    val search: String = "",
    val showAppDetailSheet: Boolean = false,
    val selectedDetailApp: AppInfo? = null,
    val showAppUsageSheet: Boolean = false,
    val showMoreSheet: Boolean = false,
    val currentStarPackageNameSet: Set<String> = setOf(),
    val hidden: Set<String> = setOf(),
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

    private var filterKeywordJob: Job? = null

    private var packageNameSet: MutableSet<String> = mutableSetOf()

    private var extensionMap: MutableMap<String, Extension> = mutableMapOf()

    private fun getExtensions(): List<Extension> {
        return extensionMap.values.toList()
    }

    fun initPackageEventReceiver(context: Context) {
        if (packageEventReceiver == null) {
            packageEventReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Timber.d("onReceive: ${intent.action}")
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
                Timber.i("onPackageAdded: ${app.label}, ${app.packageName}")
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
                mainState.copy(apps = sorted, showAppDetailSheet = false, search = "")
            }
        }
    }

    fun onPackageRemoved(intent: Intent) {
        val apps = state.value.apps.toMutableList()
        val toDeletePackageName = intent.dataString?.substring(8)
            ?: return

        val removedIndex = apps.indexOfFirst { it.packageName == toDeletePackageName }
        if (removedIndex != -1) {
            Timber.i("onPackageRemoved: removedIndex: $removedIndex, ${apps[removedIndex].label}")

            apps.removeAt(removedIndex)

            _state.update { mainState ->
                mainState.copy(apps = apps, showAppDetailSheet = false, search = "")
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
                    loadPackageNameSet(dbApps)
                    Timber.d("load from db, ${dbApps.size} apps")
                }
                // load from packageManager
                loadFromPackageManger(context, dbApps)
                loadExtensions(context)
                loadHiddenSet(context)
            }
        }
    }

    private fun loadPackageNameSet(apps: List<AppInfo>) {
        packageNameSet.clear()
        apps.forEach {
            packageNameSet.add(it.packageName)
        }
    }

    private fun loadExtensions(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                ExtensionHelper.DEFAULT_EXTENSIONS_RULES.forEach { url ->
                    ExtensionHelper.fetchExtension(context, url) { appExtensionGroup ->
                        if (appExtensionGroup != null) {
                            Timber.d("loadExtensions: ${appExtensionGroup.name}, ${appExtensionGroup.description}, ${appExtensionGroup.version}")
                        } else {
                            Timber.d("loadExtensions: null for $url")
                        }
                        if (appExtensionGroup != null && appExtensionGroup.main.isNotEmpty()) {
                            val nextExtensions = appExtensionGroup.main.filter { extension ->
                                if (extension.required != null) {
                                    return@filter extension.required.all { required ->
                                        packageNameSet.contains(
                                            required
                                        )
                                    }
                                }
                                true
                            }.map {
                                it.filterTarget =
                                    FilterHelper.toTarget(it.name, it.description)
                                it
                            }
                            nextExtensions.forEach {
                                extensionMap[it.name] = it
                            }
                        }
                    }
                }

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

                if (apps.size <= 1) {
                    Timber.d("missing permission")

                    return@withContext _state.update {
                        it.copy(missingPermission = true)
                    }
                }

                loadPackageNameSet(apps)

                _state.update { mainState ->
                    mainState.copy(
                        missingPermission = false,
                        apps = apps,
                        scrollbarItems = getScrollbarItemsFromApps(apps)
                    )
                }

                updateDbAppsPriority(context, apps)

                Timber.d("load from packageManager, ${apps.size} apps")

                checkAndCleanDbApps(context, apps, dbApps)
            }
        }
    }

    private fun checkAndCleanDbApps(context: Context, apps: List<AppInfo>, dbApps: List<AppInfo>?) {
        viewModelScope.launch {
            withContext(IO) {
                if (dbApps?.size!! > apps.size) {
                    val appsSet = apps.map { it.packageName }.toSet()
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


    fun setSearch(search: String) {
        _state.update { mainState ->
            mainState.copy(search = search)
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

    fun setShowMoreSheet(visible: Boolean) {
        _state.update { mainState ->
            mainState.copy(
                showMoreSheet = visible
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

    fun onSearch(text: String, enableExtension: Boolean, fuzzy: Boolean) {
        viewModelScope.launch {
            val search = text.lowercase()
            val apps = _state.value.apps
            val extensions = getExtensions()

            val searchResult = apps.map { SearchResultItem(it) }.toMutableList()

            if (enableExtension && extensions.isNotEmpty()) {
                searchResult.addAll(
                    extensions.map {
                        SearchResultItem(it)
                    }
                )
            }

            val filterResult = if (fuzzy) {
                searchResult
                    .map {
                        val ratio = if (it.isApp()) {
                            val app = it.asApp()!!
                            FuzzySearch.partialRatio(
                                app.abbr.lowercase() + " " + app.filterTarget.lowercase(),
                                search
                            )
                        } else if (it.isExtension()) {
                            val extension = it.asExtension()!!
                            FuzzySearch.partialRatio(
                                extension.name.lowercase() + " " + extension.filterTarget!!.lowercase(),
                                search
                            )
                        } else 0

                        Fuzzy(it, ratio)
                    }
                    .filter {
                        (it.ratio >= 50)
                    }
                    .sortedByDescending {
                        val priority = it.resultItem.getPriority()
                        it.ratio * (priority + 1)
                    }
                    .map {
                        it.resultItem
                    }
            } else {
                searchResult.filter {
                    if (it.isApp()) {
                        val app = it.asApp()!!
                        return@filter app.abbr.lowercase()
                            .contains(search) || app.filterTarget.lowercase()
                            .contains(search)
                    } else if (it.isExtension()) {
                        val extension = it.asExtension()!!
                        return@filter extension.name.lowercase()
                            .contains(search) || extension.filterTarget!!.lowercase()
                            .contains(search)
                    }
                    return@filter true
                }.sortedByDescending {
                    it.getPriority()
                }
            }

            _state.update { mainState ->
                mainState.copy(searchResult = filterResult, search = text)
            }
        }
    }

    fun filterKeywordStars(context: Context, enableExtension: Boolean, keyword: String) {
        filterKeywordJob?.cancel()
        filterKeywordJob = viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val stars = db.starDao().getKeywordStars(keyword)
                Timber.d("filterKeywordStars: ${stars.joinToString { it.packageName }}")
                val starSet = HashSet<String>()

                stars.forEach {
                    starSet.add(it.packageName)
                }

                val apps = _state.value.apps
                val extensions = getExtensions()

                val starAppList = apps.filter { starSet.contains(it.packageName) }
                    .sortedByDescending { it.priority }.map {
                        SearchResultItem(it)
                    }

                val starExtensionList = if (enableExtension && extensions.isNotEmpty())
                    extensions.filter { starSet.contains(it.name) }
                        .sortedByDescending { it.priority }.map {
                            SearchResultItem(it)
                        }
                else listOf()

                val restList = _state.value.searchResult.filter {
                    if (it.isApp()) {
                        return@filter !starSet.contains(it.asApp()!!.packageName)
                    }
                    return@filter !starSet.contains(it.asExtension()!!.name)
                }

                _state.update { mainState ->
                    mainState.copy(
                        currentStarPackageNameSet = starSet,
                        searchResult = starExtensionList.plus(starAppList.plus(restList))
                    )
                }
            }
        }
    }

    fun starApp(
        context: Context,
        enableExtension: Boolean,
        packageName: String,
        keyword: String,
        isStar: Boolean
    ) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                if (!isStar) {
                    db.starDao().insertStar(Star(packageName = packageName, keyword = keyword))
                } else {
                    db.starDao().deleteStarPackage(packageName, keyword)
                }
                filterKeywordStars(context, enableExtension, keyword = keyword)
            }
        }
    }

    fun getAllStars(context: Context): List<Star> {
        val db = getDatabase(context)
        return db.starDao().getAllStars()
    }

    fun deleteStar(context: Context, star: Star) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                db.starDao().deleteStar(star)
            }
        }
    }

    fun updateExtensionPriority(extension: Extension) {
        viewModelScope.launch {
            withContext(IO) {
                if (extensionMap.containsKey(extension.name)) {
                    extension.priority += 1
                    extensionMap[extension.name] = extension
                }
            }
        }
    }

    fun getHiddenList(context: Context): List<Hidden> {
        return getDatabase(context).hiddenDao().getAll()
    }

    fun deleteHidden(context: Context, hidden: Hidden) {
        viewModelScope.launch {
            withContext(IO) {
                getDatabase(context).hiddenDao().delete(hidden)
                loadApps(context)
            }
        }
    }

    fun insertHidden(context: Context, name: String) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val hiddenDao = db.hiddenDao()

                hiddenDao.insert(Hidden(name))

                loadHiddenSet(context)
            }
        }
    }

    private fun loadHiddenSet(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val hiddenDao = db.hiddenDao()

                val data = mutableSetOf<String>()

                hiddenDao.getAll().forEach {
                    data.add(it.key)
                }

                val apps = _state.value.apps

                _state.update { mainState ->
                    mainState.copy(hidden = data, apps = apps.filter { !data.contains(it.packageName) })
                }
            }
        }
    }

    private fun getScrollbarItemsFromApps(apps: List<AppInfo>): List<String> {
        return apps.map { it.abbr.first().uppercase() }
            .toSet().toList().sorted()
    }
}

data class Fuzzy(val resultItem: SearchResultItem, val ratio: Int)