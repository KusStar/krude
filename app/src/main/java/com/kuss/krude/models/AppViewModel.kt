package com.kuss.krude.models

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kuss.krude.data.AppInfo

class AppViewModel: ViewModel() {
    val allApps: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val apps: MutableLiveData<List<AppInfo>> = MutableLiveData()
    val search: MutableLiveData<String> = MutableLiveData()

    fun clearApps() {
        apps.value = emptyList()
    }

    fun clearSearch() {
        search.value = ""
    }

}