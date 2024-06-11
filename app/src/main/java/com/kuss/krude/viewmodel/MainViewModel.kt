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
import com.kuss.krude.db.ExtensionCache
import com.kuss.krude.db.Hidden
import com.kuss.krude.db.Star
import com.kuss.krude.db.Usage
import com.kuss.krude.db.UsageCountByDay
import com.kuss.krude.extensions.FILES_EXTENSION
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.ExtensionHelper
import com.kuss.krude.utils.ExtensionHelper.overwriteI18nExtension
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.LocaleHelper
import com.kuss.krude.utils.ToastUtils
import com.kuss.krude.viewmodel.settings.SettingsState
import com.kuss.krude.viewmodel.settings.SettingsViewModel
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
    val originalApps: List<AppInfo> = listOf(),
    val apps: List<AppInfo> = listOf(),
    val searchResult: List<SearchResultItem> = listOf(),
    val scrollbarItems: List<String> = listOf(),
    val currentScrollbarIndex: Int = 0,
    val showAppDetailSheet: Boolean = false,
    val selectedDetailApp: AppInfo? = null,
    val showAppUsageSheet: Boolean = false,
    val showMoreSheet: Boolean = false,
    val currentStarPackageNameSet: Set<String> = setOf(),
    val hidden: Set<String> = setOf(),
    val extensionMap: Map<String, Extension> = mapOf()
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

    private lateinit var settingsViewModel: SettingsViewModel

    private var loadExtensionsJob: Job? = null

    private fun getExtensionsWithInternal(): List<Extension> {
        return _state.value.extensionMap.values.toList().plus(FILES_EXTENSION)
    }

    private fun getExtensions(): List<Extension> {
        return _state.value.extensionMap.values.toList()
    }

    private fun getSettingsState(): SettingsState {
        return settingsViewModel.state.value
    }

    fun initSettingsViewModel(settingsViewModel: SettingsViewModel) {
        this.settingsViewModel = settingsViewModel
    }

    fun unregisterPackageEventReceiver(context: Context) {
        if (packageEventReceiver != null) {
            try {
                context.unregisterReceiver(packageEventReceiver)
                packageEventReceiver = null
                Timber.d("unregisterPackageEventReceiver")
            } catch (_: Exception) {

            }
        }
    }

    fun initPackageEventReceiver(context: Context) {
        if (packageEventReceiver == null) {
            packageEventReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    Timber.d("packageEventReceiver onReceive: ${intent.action}")
                    when (intent.action) {
                        Intent.ACTION_PACKAGE_ADDED -> {
                            onPackageAdded(context, intent)
                        }

                        Intent.ACTION_PACKAGE_REMOVED -> {
                            onPackageRemoved(intent)
                        }

                        Intent.ACTION_PACKAGE_REPLACED -> {
                            loadApps(context)
                        }

                        Intent.ACTION_PACKAGE_CHANGED -> {
                            loadApps(context)
                        }
                    }
                }
            }

            val filter = IntentFilter()
            filter.addAction(Intent.ACTION_PACKAGE_ADDED)
            filter.addAction(Intent.ACTION_PACKAGE_REMOVED)
            filter.addAction(Intent.ACTION_PACKAGE_REPLACED)
            filter.addAction(Intent.ACTION_PACKAGE_CHANGED)
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
                mainState.copy(apps = sorted, showAppDetailSheet = false)
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
                mainState.copy(apps = apps, showAppDetailSheet = false)
            }
        }
    }

    fun loadApps(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                Timber.d("loadApps")
                // load from db
                val db = getDatabase(context)

                val dbApps = db.appDao().getAllApps()

                if (dbApps.isNotEmpty()) {
                    _state.update { mainState ->
                        mainState.copy(
                            originalApps = dbApps,
                            apps = dbApps,
                            scrollbarItems = getScrollbarItemsFromApps(dbApps)
                        )
                    }
                    postLoadApps(context, dbApps)
                    Timber.d("load from db, ${dbApps.size} apps")
                }
                // load from packageManager
                loadPackageNameSet(context)
                loadFromPackageManger(context, dbApps)
                loadExtensions(context)
            }
        }
    }

    private fun postLoadApps(context: Context, apps: List<AppInfo>) {
        loadHiddenSet(context, apps)
    }

    private fun loadPackageNameSet(context: Context) {
        packageNameSet.addAll(AppHelper.getAllPackageNames(context))
    }

    fun clearExtensionsCache(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                db.extensionCacheDao().deleteAll()
            }
        }
    }

    fun loadExtensions(context: Context) {
        loadExtensionsJob?.cancel()
        loadExtensionsJob = viewModelScope.launch {
            withContext(IO) {
                _state.update {
                    it.copy(extensionMap = mapOf())
                }
                loadExtensionsFromCache(context)

                val settingsState = getSettingsState()
                val repoUrl = if (settingsState.devExtension) {
                    settingsState.devExtensionRepo
                } else {
                    ExtensionHelper.EXTENSIONS_REPO
                }
                Timber.i("loadExtensions: $repoUrl")
                val (exception, extensionUrls) = ExtensionHelper.fetchExtensionsFromRepo(
                    context,
                    repoUrl
                )
                if (exception != null) {
                    Timber.e("loadExtensions: error, $exception")
                    ToastUtils.show(
                        context,
                        "Load extensions error, please check the repo url."
                    )
                    return@withContext
                }
                extensionUrls?.forEach { url ->
                    ExtensionHelper.fetchExtension(context, url) { appExtensionGroup ->
                        if (appExtensionGroup != null) {
                            Timber.d("loadExtensions: ${appExtensionGroup.name}, ${appExtensionGroup.description}, ${appExtensionGroup.main.size}")
                        } else {
                            Timber.d("loadExtensions: null for $url")
                        }
                        if (appExtensionGroup != null && appExtensionGroup.main.isNotEmpty()) {
                            val nextExtensions = appExtensionGroup.main.filter { extension ->
                                if (extension.type == ExtensionType.ALIAS) {
                                    return@filter false
                                }
                                if (!extension.required.isNullOrEmpty()) {
                                    return@filter extension.required!!.any { required ->
                                        packageNameSet.contains(
                                            required
                                        )
                                    }
                                }
                                true
                            }.map {
                                if (it.i18n != null) {
                                    if (LocaleHelper.currentLocale == "zh" && it.i18n.zh != null) {
                                        overwriteI18nExtension(it, it.i18n.zh)
                                    }
                                    if (LocaleHelper.currentLocale == "en" &&it.i18n.en != null) {
                                        overwriteI18nExtension(it, it.i18n.en)
                                    }
                                }
                                it.filterTarget =
                                    FilterHelper.toTarget(it)
                                if (!it.required.isNullOrEmpty()) {
                                    it.required = it.required!!.sortedByDescending { re ->
                                        packageNameSet.contains(re)
                                    }
                                    // Format "设置-WiFi" to "WiFi", no need to show prefix when required package icon is shown
                                    it.name = if (it.name.contains("-")) it.name.split(
                                        "-",
                                        limit = 2
                                    )[1] else it.name
                                }

                                it
                            }
                            // bind alias extensions to apps
                            if (nextExtensions.isNotEmpty()) {
                                val tempMap = _state.value.extensionMap.toMutableMap()
                                nextExtensions.forEach {
                                    tempMap[it.id] = it
                                }
                                _state.update {
                                    it.copy(extensionMap = tempMap)
                                }
                                saveExtensionsIntoCache(context, nextExtensions)
                            }
                            val aliasExtensions = appExtensionGroup.main.filter { it.type == ExtensionType.ALIAS && !it.required.isNullOrEmpty() }
                            if (aliasExtensions.isNotEmpty()) {
                                Timber.d("loadExtensions: aliasExtensions ${aliasExtensions.size}")
                                bindAliasFilterTarget(aliasExtensions)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun bindAliasFilterTarget(aliasExtensions: List<Extension>) {
        val apps = _state.value.apps
        val extensions = getExtensions()
        if (extensions.isNotEmpty()) {
            extensions.forEach { extension ->
                val aliasSet = aliasExtensions.find {
                    it.required!!.intersect((extension.required ?: listOf()).toSet()).isNotEmpty()
                }
                if (aliasSet != null) {
                    extension.filterTarget = extension.filterTarget + "," + FilterHelper.keywordsToTarget(aliasSet)
                }
            }
        }
        _state.update { mainState ->
            mainState.copy(
                extensionMap = extensions.associateBy { it.id },
                apps = apps.map { app ->
                    val aliasExtension = aliasExtensions.find { it.required!!.contains(app.packageName) }
                    if (aliasExtension != null) {
                        return@map app.copy(
                            filterTarget = app.filterTarget + "," + FilterHelper.keywordsToTarget(aliasExtension)
                        )
                    }
                    app
                },
            )
        }
    }

    private fun loadExtensionsFromCache(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val extensionCacheDao = db.extensionCacheDao()
                val tempMap = mutableMapOf<String, Extension>()
                extensionCacheDao.getAll().forEach {
                    tempMap[it.id] = it.extension
                }
                _state.update {
                    it.copy(extensionMap = tempMap)
                }
            }
        }
    }

    private fun saveExtensionsIntoCache(context: Context, extensions: List<Extension>) {
        viewModelScope.launch {
            withContext(IO) {
                if (extensions.isEmpty()) {
                    Timber.d("cacheExtensions skip, extensions is empty")
                    return@withContext
                }
                val db = getDatabase(context)
                val extensionCacheDao = db.extensionCacheDao()
                extensions.forEach {
                    extensionCacheDao.insert(ExtensionCache(id = it.id, extension = it))
                }
                Timber.d("cacheExtensions done, ${extensions.size}")
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


                _state.update { mainState ->
                    mainState.copy(
                        missingPermission = false,
                        originalApps = apps,
                        apps = apps,
                        scrollbarItems = getScrollbarItemsFromApps(apps)
                    )
                }

                postLoadApps(context, apps)

                updateDbAppsPriority(context, apps)

                Timber.d("load from packageManager, ${apps.size} apps")

                checkAndCleanDbApps(context, apps, dbApps)
            }
        }
    }

    private fun checkAndCleanDbApps(context: Context, apps: List<AppInfo>, dbApps: List<AppInfo>?) {
        viewModelScope.launch {
            withContext(IO) {
                if (dbApps != null) {
                    if (dbApps.size > apps.size) {
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
            val extensions = getExtensionsWithInternal()

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
                            val filterTargetLower = extension.filterTarget?.lowercase() ?: ""
                            FuzzySearch.partialRatio(
                                extension.name.lowercase() + " " + filterTargetLower,
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
                        val nameContains = extension.name.lowercase().contains(search)
                        val filterTargetContains = extension.filterTarget?.lowercase()?.contains(search)
                        return@filter  nameContains || filterTargetContains == true
                    }
                    return@filter true
                }.sortedByDescending {
                    it.getPriority()
                }
            }

            _state.update { mainState ->
                mainState.copy(searchResult = filterResult)
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
                val extensions = getExtensionsWithInternal()

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
                val tempMap = _state.value.extensionMap.toMutableMap()
                if (tempMap.containsKey(extension.name)) {
                    extension.priority += 1
                    tempMap[extension.name] = extension
                    _state.update {
                        it.copy(extensionMap = tempMap)
                    }
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
                getDatabase(context).hiddenDao().delete(hidden.key)
                loadApps(context)
            }
        }
    }

    fun insertHidden(context: Context, name: String) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val hiddenDao = db.hiddenDao()

                hiddenDao.delete(name)

                hiddenDao.insert(Hidden(name))

                loadHiddenSet(context, _state.value.apps)
            }
        }
    }

    private fun loadHiddenSet(context: Context, apps: List<AppInfo>) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val hiddenDao = db.hiddenDao()
                val hiddenSet = hiddenDao.getAll().mapTo(HashSet()) { it.key }

                _state.update { mainState ->
                    mainState.copy(
                        hidden = hiddenSet,
                        apps = apps.filter { !hiddenSet.contains(it.packageName) })
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