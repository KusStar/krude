package com.kuss.krude.viewmodel.extensions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kuss.krude.shizuku.bean.BeanFile
import com.kuss.krude.ui.components.internal.files.FileHelper
import com.kuss.krude.ui.components.internal.files.PathNavigator
import com.kuss.krude.ui.components.internal.files.WAIT_TIME
import com.kuss.krude.utils.FilterHelper
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File

enum class FilesOrderBy {
    ALPHABET_ASC,
    ALPHABET_DESC,
    DATE_ASC,
    DATE_DESC,
    SIZE_ASC,
    SIZE_DESC
}

data class FilesExtensionState(
    val files: List<BeanFile> = listOf(),
    val filteredFiles: List<BeanFile> = listOf(),
    val search: String = "",
    val pathNavigator: PathNavigator = PathNavigator(),
    val tabs: List<String> = listOf(FileHelper.ROOT_PATH),
    val currentTabIndex: Int = 0,
    val showHiddenFiles: Boolean = false,
    val filesOrderBy: FilesOrderBy = FilesOrderBy.ALPHABET_ASC
)

class FilesExtensionViewModel : ViewModel() {
    private val _state = MutableStateFlow(FilesExtensionState())

    val state: StateFlow<FilesExtensionState>
        get() = _state

    private fun setFiles(files: List<BeanFile>) {
        _state.update {
            it.copy(files = files)
        }
    }

    fun setFilteredFiles(files: List<BeanFile>) {
        _state.update {
            it.copy(filteredFiles = files)
        }
    }

    fun setSearch(search: String) {
        _state.update { it.copy(search = search) }
    }

    fun updateCurrentTab() {
        _state.update {
            val nextTabs = it.tabs.toMutableList()
            nextTabs[it.currentTabIndex] = it.pathNavigator.currentPath
            it.copy(
                tabs = nextTabs
            )
        }
    }

    fun setTab(tabs: List<String>) {
        _state.update {
            it.copy(
                tabs = tabs
            )
        }
    }

    private fun addTab(path: String) {
        _state.update {
            val nextTabs = it.tabs.toMutableList()
            nextTabs.add(path)
            it.copy(
                tabs = nextTabs
            )
        }
    }

    fun removeTab(index: Int) {
        _state.update {
            val nextTabs = it.tabs.toMutableList()
            nextTabs.removeAt(index)
            it.copy(
                tabs = nextTabs
            )
        }
    }

    fun setCurrentTabIndex(index: Int) {
        _state.update {
            it.copy(currentTabIndex = index)
        }
    }

    fun newTab(path: String, jump: Boolean, jumpedCallback: (() -> Unit)? = null) {
        addTab(path)
        if (jump) {
            // stupid workaround for IndexOutOfBoundsException
            viewModelScope.launch {
                delay(WAIT_TIME)
                setCurrentTabIndex(_state.value.tabs.lastIndex)
                jumpedCallback?.invoke()
            }
        }
    }

    fun goToPath(path: String) {
        setSearch("")
        _state.value.pathNavigator.goTo(path)
        updateCurrentTab()
    }

    fun goForward() {
        setSearch("")
        goForward()
        updateCurrentTab()
    }

    fun loadFiles(path: String) {
        viewModelScope.launch {
            withContext(IO) {
                val list = FileHelper.listFiles(File(path))?.toList() ?: emptyList()
                setFiles(list.sortedBy { FilterHelper.getAbbr(it.name.lowercase()) })
                Timber.d("Path: $path, Files: $list")
            }
        }
    }

    fun closeTab(index: Int) {
        val currentTabIndex = _state.value.currentTabIndex
        removeTab(index)
        if (currentTabIndex > 0) {
            _state.update {
                it.copy(
                    currentTabIndex = currentTabIndex - 1,
                )
            }
            goToPath(_state.value.tabs[currentTabIndex - 1])
        }
    }

    fun setShowHiddenFiles(show: Boolean) {
        _state.update {
            it.copy(showHiddenFiles = show)
        }
    }

    fun setFilesOrderBy(orderBy: FilesOrderBy) {
        _state.update {
            it.copy(filesOrderBy = orderBy)
        }
    }
}