package com.kuss.krude.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppViewModel: ViewModel() {
    private var allApps: List<AppInfo> = emptyList()
    val apps: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val search: MutableLiveData<String> = MutableLiveData()

    fun setAllApps(value: List<AppInfo>) {
        allApps = value
    }

    fun getAllApps(): List<AppInfo> {
        return allApps
    }

    fun clearApps() {
        apps.value = emptyList()
    }

    fun clearSearch() {
        search.value = ""
    }

}