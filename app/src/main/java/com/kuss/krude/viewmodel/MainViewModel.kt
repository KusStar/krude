package com.kuss.krude.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.kuss.krude.data.AppInfo
import com.kuss.krude.db.AppDatabase
import com.kuss.krude.utils.AppHelper
import com.kuss.krude.utils.TAG
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class MainState(
    val apps: List<AppInfo> = listOf(),
    val filteredApps: List<AppInfo> = listOf(),
    val scrollbarItems: List<String> = listOf(),
    val currentScrollbarIndex: Int = 0,
    val filtering: String = "",
    val showAppDetailSheet: Boolean = false,
    val selectedDetailApp: AppInfo? = null
)

class MainViewModel : ViewModel() {

    companion object {
        @Volatile
        private var DB: AppDatabase? = null

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
                val items = AppHelper.getInstalled(context)

                db.appDao().deleteAllApp()

                items.forEach {
                    db.appDao().insertApp(
                        it
                    )
                }

                _state.update { mainState ->
                    mainState.copy(
                        apps = items,
                        scrollbarItems = getScrollbarItemsFromApps(items)
                    )
                }

                Log.d(TAG, "load from packageManager, ${items.size} apps")
            }
        }
    }

    fun setFiltering(filtering: String) {
        _state.update { mainState ->
            mainState.copy(filtering = filtering)
        }
    }

    fun setFilteredApps(apps: List<AppInfo>) {
        _state.update { mainState ->
            mainState.copy(filteredApps = apps)
        }
    }

    fun setShowAppDetailSheet(visible: Boolean) {
        _state.update { mainState ->
            mainState.copy(
                showAppDetailSheet = visible
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

    private fun getScrollbarItemsFromApps(apps: List<AppInfo>): List<String> {
        return apps.map { it.abbr.first().uppercase() }
            .toSet().toList().sorted()
    }
}