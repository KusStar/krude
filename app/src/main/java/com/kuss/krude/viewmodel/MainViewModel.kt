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
import com.kuss.krude.db.UsageDao
import com.kuss.krude.interfaces.Extension
import com.kuss.krude.interfaces.ExtensionType
import com.kuss.krude.interfaces.SearchResultItem
import com.kuss.krude.ui.StarItemState
import com.kuss.krude.ui.components.MessageBarState
import com.kuss.krude.ui.components.internal.InternalExtensions
import com.kuss.krude.utils.ActivityHelper
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.ExtensionHelper
import com.kuss.krude.utils.ExtensionHelper.overwriteI18nExtension
import com.kuss.krude.utils.FilterHelper
import com.kuss.krude.utils.LocaleHelper
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
import kotlin.time.Duration

data class MainState(
    val missingPermission: Boolean = false,
    val originalApps: List<AppInfo> = listOf(),
    val apps: List<AppInfo> = listOf(),
    val searchResult: List<SearchResultItem> = listOf(),
    val scrollbarItems: List<String> = listOf(),
    val currentScrollbarIndex: Int = 0,
    val showAppUsageSheet: Boolean = false,
    val showMoreSheet: Boolean = false,
    val keywordStarSet: Set<String> = setOf(),
    val hidden: Set<String> = setOf(),
    val extensionMap: Map<String, Extension> = mapOf()
)

data class AppStatsModalState(
    val appInfo: AppInfo? = null,
    val visible: Boolean = false,
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

    val state: StateFlow<MainState>
        get() = _state


    private val _starItemState = MutableStateFlow(StarItemState())

    val starItemState: StateFlow<StarItemState>
        get() = _starItemState

    private val _appStatsModalState = MutableStateFlow(AppStatsModalState())

    val appStatsModalState: StateFlow<AppStatsModalState>
        get() = _appStatsModalState

    private lateinit var messageBarState: MessageBarState

    private var packageNameSet: MutableSet<String> = mutableSetOf()

    // star map keyword to key
    private var allStarMap: Map<String, String> = mapOf()

    private lateinit var settingsViewModel: SettingsViewModel

    private var loadExtensionsJob: Job? = null

    // every required id to keyword
    private val aliasKeywordMap = mutableMapOf<String, String>()

    fun setStarItemDialogVisible(visible: Boolean, item: SearchResultItem? = null) {
        _starItemState.update {
            it.copy(visible = visible, item = item)
        }
    }

    fun getMessageBarState(): MessageBarState {
        return messageBarState
    }

    fun initMessageBarState(messageBarState: MessageBarState) {
        this.messageBarState = messageBarState
    }

    private fun parseExtension(it: Extension): Extension {
        if (it.i18n != null) {
            if (LocaleHelper.currentLocale == "zh" && it.i18n.zh != null) {
                overwriteI18nExtension(it, it.i18n.zh)
            }
            if (LocaleHelper.currentLocale == "en" && it.i18n.en != null) {
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
        return it
    }

    fun getExtensionsWithInternal(): List<Extension> {
        return _state.value.extensionMap.values.toList()
            .plus(InternalExtensions.ALL.map { parseExtension(it) })
    }

    private fun getExtensions(): List<Extension> {
        return _state.value.extensionMap.values.toList()
    }

    fun getSettingsState(): SettingsState {
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
                mainState.copy(apps = sorted)
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
                mainState.copy(apps = apps)
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
                loadAllStarMap(context)
            }
        }
    }

    // onResume - reload apps from system
    fun reloadAppsFromSystem(context: Context) {
        if (state.value.originalApps.isEmpty()) {
            return
        }
        // load from packageManager
        loadPackageNameSet(context)
        loadFromPackageManger(context, null)
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

    fun loadExtensions(context: Context, showMessage: Boolean = false) {
        loadExtensionsJob?.cancel()
        loadExtensionsJob = viewModelScope.launch {
            withContext(IO) {
                _state.update {
                    it.copy(extensionMap = mapOf())
                }
                aliasKeywordMap.clear()

                loadExtensionsFromCache(context)

                val settingsState = getSettingsState()
                val repoUrl = if (settingsState.devExtension) {
                    settingsState.devExtensionRepo
                } else {
                    ExtensionHelper.EXTENSIONS_REPO
                }
                Timber.i("loadExtensions from repo")
                if (showMessage) {
                    messageBarState.showLoading("Loading extensions from repo")
                }
                val (exception, extensionRepos) = ExtensionHelper.fetchExtensionsFromRepo(
                    context,
                    repoUrl
                )
                if (exception != null) {
                    Timber.e("loadExtensions: error, $exception")
                    if (showMessage) {
                        messageBarState.showError("Load extensions error, please check the repo url.")
                    }
                    return@withContext
                }
                if (extensionRepos != null) {
                    var success = 0
                    var failed = 0
                    for ((index, extensionRepo) in extensionRepos.withIndex()) {
                        if (showMessage) {
                            messageBarState.showLoading("(${index + 1}/${extensionRepos.size}) Loading ${extensionRepo.name}")
                        }
                        ExtensionHelper.fetchExtension(
                            context,
                            extensionRepo.url
                        ) { appExtensionGroup ->
                            if (appExtensionGroup == null) {
                                if (showMessage) {
                                    messageBarState.showError("Cannot load ${extensionRepo.name}")
                                }
                                failed += 1
                            } else {
                                if (appExtensionGroup.main.isNotEmpty()) {
                                    val (aliasesData, extensionsData) = appExtensionGroup.main.partition { it.type == ExtensionType.ALIAS }
                                    val nextExtensions = extensionsData.filter { extension ->
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
                                            if (LocaleHelper.currentLocale == "en" && it.i18n.en != null) {
                                                overwriteI18nExtension(it, it.i18n.en)
                                            }
                                        }
                                        it.filterTarget =
                                            FilterHelper.toTarget(it)
                                        if (!it.required.isNullOrEmpty()) {
                                            it.required =
                                                it.required!!.sortedByDescending { re ->
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
                                    val aliasExtensions =
                                        aliasesData.filter { !it.required.isNullOrEmpty() }
                                    if (aliasExtensions.isNotEmpty()) {
                                        Timber.d("loadExtensions: aliasExtensions ${aliasExtensions.size}")
                                        setAlias(aliasExtensions)
                                        saveExtensionsIntoCache(context, aliasExtensions)
                                    }
                                    success += 1
                                }
                            }
                            if (success + failed == extensionRepos.size) {
                                if (showMessage) {
                                    messageBarState.showSuccess(
                                        message = "Loaded from $success extension repo",
                                        duration = Duration.parse("1s")
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }
    }

    private fun setAlias(aliasExtensions: List<Extension>) {
        aliasExtensions.forEach {
            val keywords = FilterHelper.keywordsToTarget(it)
            it.required?.forEach { id ->
                aliasKeywordMap[id] = keywords
            }
        }
    }

    private fun loadExtensionsFromCache(context: Context) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                val extensionCacheDao = db.extensionCacheDao()
                val tempMap = mutableMapOf<String, Extension>()
                val (aliasesData, extensionsData) = extensionCacheDao.getAll()
                    .partition { it.extension.type == ExtensionType.ALIAS }
                if (aliasesData.isNotEmpty()) {
                    val aliasExtensions = aliasesData.map { it.extension }.filter {
                        !it.required.isNullOrEmpty()
                    }
                    setAlias(aliasExtensions)
                }
                if (extensionsData.isNotEmpty()) {
                    extensionsData.forEach {
                        tempMap[it.id] = it.extension
                    }
                    _state.update {
                        it.copy(extensionMap = tempMap)
                    }
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

    fun getAppsByDay(context: Context, day: String): List<UsageDao.AppInfoWithUsage> {
        val db = getDatabase(context)

        return db.usageDao().getAppsByDay(day)
    }

    private fun loadFromPackageManger(context: Context, dbApps: List<AppInfo>? = null) {
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

    fun setShowAppStatsModal(visible: Boolean, app: AppInfo? = null) {
        _appStatsModalState.update { state ->
            state.copy(
                visible = visible,
                appInfo = app
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

    fun setSelectedHeaderIndex(i: Int) {
        _state.update { mainState ->
            mainState.copy(
                currentScrollbarIndex = i
            )
        }
    }

    private fun getAppAliasKeyword(app: AppInfo): String {
        return if (aliasKeywordMap.contains(app.packageName)) " " + aliasKeywordMap[app.packageName] else ""
    }

    private fun getExtensionAliasKeyword(extension: Extension): String {
        return if (extension.required != null) {
            val found =
                extension.required!!.find { aliasKeywordMap.contains(it) }
            if (aliasKeywordMap.contains(found)) " " + aliasKeywordMap[found] else ""
        } else ""
    }

    fun clearSearch() {
        _state.update { mainState ->
            mainState.copy(searchResult = listOf())
        }
    }

    fun onSearch(text: String, enableExtension: Boolean, fuzzy: Boolean) {
        viewModelScope.launch {
            if (text.isEmpty()) {
                clearSearch()
                return@launch
            }
            val search = text.lowercase()
            val apps = _state.value.apps
            val extensions = getExtensionsWithInternal()

            val searchData = apps.map { SearchResultItem(it) }.toMutableList()

            if (enableExtension && extensions.isNotEmpty()) {
                searchData.addAll(
                    extensions.map {
                        SearchResultItem(it)
                    }
                )
            }

            val filterResult = filterSearchData(search, searchData, fuzzy)

            val (starSet, finalResult) = filterKeywordStars(
                filterResult,
                enableExtension,
                text
            )

            _state.update { mainState ->
                mainState.copy(keywordStarSet = starSet, searchResult = finalResult)
            }
        }
    }

    private fun filterSearchData(
        search: String,
        searchData: List<SearchResultItem>,
        fuzzy: Boolean
    ): List<SearchResultItem> {
        val filterResult = if (fuzzy) {
            // fuzzy search
            searchData
                .map { it ->
                    val ratio = if (it.isApp()) {
                        val app = it.asApp()!!
                        val appAliasKeyword = getAppAliasKeyword(app)
                        FuzzySearch.partialRatio(
                            app.abbr.lowercase() + " " + app.filterTarget.lowercase() + appAliasKeyword.lowercase(),
                            search
                        )
                    } else if (it.isExtension()) {
                        val extension = it.asExtension()!!
                        val extensionAliasKeyword = getExtensionAliasKeyword(extension)
                        val filterTargetLower = extension.filterTarget?.lowercase() ?: ""
                        FuzzySearch.partialRatio(
                            extension.name.lowercase() + " " + filterTargetLower + extensionAliasKeyword,
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
            // exact search
            searchData.filter {
                if (it.isApp()) {
                    val app = it.asApp()!!
                    val appAliasKeyword = getAppAliasKeyword(app)

                    val abbrContains = app.abbr.lowercase()
                        .contains(search)
                    val filterTargetContains = app.filterTarget.lowercase()
                        .contains(search)
                    val aliasContains = appAliasKeyword.lowercase().contains(search)

                    return@filter abbrContains || filterTargetContains || aliasContains
                } else if (it.isExtension()) {
                    val extension = it.asExtension()!!
                    val extensionAliasKeyword = getExtensionAliasKeyword(extension)

                    val aliasContains = extensionAliasKeyword.lowercase().contains(search)
                    val nameContains = extension.name.lowercase().contains(search)
                    val filterTargetContains =
                        extension.filterTarget?.lowercase()?.contains(search)

                    return@filter nameContains || filterTargetContains == true || aliasContains
                }
                return@filter true
            }.sortedByDescending {
                it.getPriority()
            }
        }
        return filterResult
    }

    private fun loadAllStarMap(context: Context) {
        val db = getDatabase(context)
        allStarMap = db.starDao().getAllStars().associate { it.key to it.keyword }
    }

    private fun filterKeywordStars(
        searchResult: List<SearchResultItem>,
        enableExtension: Boolean,
        keyword: String
    ): Pair<Set<String>, List<SearchResultItem>> {
        val starSet = allStarMap.filter { it.value == keyword }.keys.toSet()
        val apps = _state.value.apps
        val extensions = getExtensionsWithInternal()

        val starAppList = apps.filter { starSet.contains(it.packageName) }
            .sortedByDescending { it.priority }.map {
                SearchResultItem(it)
            }

        val starExtensionList =
            if (enableExtension && extensions.isNotEmpty())
                extensions
                    .filter { extension ->
                        starSet.contains(extension.id)
                    }
                    .sortedByDescending { it.priority }
                    .map {
                        SearchResultItem(it)
                    }
            else listOf()

        val restList = searchResult.filter {
            if (it.isApp()) {
                return@filter !starSet.contains(it.asApp()!!.packageName)
            }
            return@filter !starSet.contains(it.asExtension()!!.id)
        }

        return Pair(starSet, starExtensionList.plus(starAppList.plus(restList)))
    }

    private fun updateStarSet(context: Context, keyword: String) {
        loadAllStarMap(context)
        val (starSet, result) = filterKeywordStars(
            _state.value.searchResult,
            true,
            keyword
        )
        _state.update { mainState ->
            mainState.copy(keywordStarSet = starSet, searchResult = result)
        }
    }

    fun insertStar(
        context: Context,
        key: String,
        keyword: String,
        isStar: Boolean
    ) {
        viewModelScope.launch {
            withContext(IO) {
                val db = getDatabase(context)
                if (!isStar) {
                    db.starDao().insertStar(Star(key = key, keyword = keyword))
                } else {
                    db.starDao().deleteStarPackage(key, keyword)
                }
                updateStarSet(context, keyword)
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
                db.starDao().deleteStar(star.key)
                loadAllStarMap(context)
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