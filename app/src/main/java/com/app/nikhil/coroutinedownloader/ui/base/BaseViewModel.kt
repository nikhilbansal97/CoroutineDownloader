package com.app.nikhil.coroutinedownloader.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

abstract class BaseViewModel : ViewModel() {

    private val viewModelJob = SupervisorJob()
    val viewModelScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}