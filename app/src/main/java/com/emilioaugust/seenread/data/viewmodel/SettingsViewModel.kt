package com.emilioaugust.seenread.data.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.emilioaugust.seenread.data.datastore.SettingsDataStore
import com.emilioaugust.seenread.ui.enums.AppLanguage
import com.emilioaugust.seenread.ui.theme.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val dataStore: SettingsDataStore) : ViewModel() {
    val themeMode = dataStore.themeMode
    val language =
        dataStore.language
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                AppLanguage.ENGLISH
            )

    val openInExternalBrowser = dataStore
        .openInExternalBrowser
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    val autoFillCopiedUrlFlow = dataStore
        .autoFillCopiedUrlFlow
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            false
        )

    val isFirstLaunch: StateFlow<Boolean?> = dataStore.isFirstLaunch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun setFirstLaunchCompleted() {
        viewModelScope.launch {
            dataStore.setFirstLaunchCompleted()
        }
    }

    fun setOpenInExternalBrowser(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setOpenInExternalBrowser(enabled)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            dataStore.setThemeMode(mode)
        }
    }

    suspend fun setLanguage(language: AppLanguage) {
        dataStore.setAppLanguage(language)
    }

    fun setAutoFillCopiedUrl(enabled: Boolean) {
        viewModelScope.launch {
            dataStore.setAutoFillCopiedUrl(enabled)
        }
    }
}

class SettingsViewModelFactory(private val dataStore: SettingsDataStore) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsViewModel(dataStore) as T
    }
}