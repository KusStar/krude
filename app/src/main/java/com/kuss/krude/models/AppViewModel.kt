package com.kuss.krude.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AppViewModel: ViewModel() {
    private var allApps: List<AppInfo> = emptyList()
    val apps: MutableLiveData<List<AppInfo>> = MutableLiveData()

    fun setAllApps(value: List<AppInfo>) {
        allApps = value
    }

    fun getAllApps(): List<AppInfo> {
        return allApps
    }

}