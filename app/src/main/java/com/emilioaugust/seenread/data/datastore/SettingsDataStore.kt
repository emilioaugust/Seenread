package com.emilioaugust.seenread.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.emilioaugust.seenread.ui.enums.AppLanguage
import com.emilioaugust.seenread.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(
    name = "settings"
)

class SettingsDataStore(private val context: Context) {
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val APP_LANGUAGE_KEY = stringPreferencesKey("app_language")
        private val OPEN_IN_EXTERNAL_BROWSER = booleanPreferencesKey("open_in_external_browser")
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val AUTO_FILL_COPIED_URL = booleanPreferencesKey("auto_fill_copied_url")
    }

    val isFirstLaunch: Flow<Boolean> = context.dataStore.data
        .map { it[KEY_FIRST_LAUNCH] ?: true }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        ThemeMode.valueOf(
            it[THEME_MODE]
                ?: ThemeMode.SYSTEM.name
        )
    }

    val language: Flow<AppLanguage> =
        context.dataStore.data.map { preferences ->
            val value = preferences[APP_LANGUAGE_KEY] ?: AppLanguage.ENGLISH.name

            AppLanguage.entries.firstOrNull { it.name == value }
                ?: AppLanguage.ENGLISH
        }

    val openInExternalBrowser: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[OPEN_IN_EXTERNAL_BROWSER] ?: false
        }

    val autoFillCopiedUrlFlow: Flow<Boolean> =
        context.dataStore.data.map { preferences ->
            preferences[AUTO_FILL_COPIED_URL] ?: true
        }

    suspend fun setFirstLaunchCompleted() {
        context.dataStore.edit { it[KEY_FIRST_LAUNCH] = false }
    }

    suspend fun setOpenInExternalBrowser(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[OPEN_IN_EXTERNAL_BROWSER] = enabled
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit {
            it[THEME_MODE] = mode.name
        }
    }

    suspend fun setAppLanguage(language: AppLanguage) {
        context.dataStore.edit {
            it[APP_LANGUAGE_KEY] = language.name
        }
    }

    suspend fun getAppLanguage(): AppLanguage {
        val value = context.dataStore.data
            .map { it[APP_LANGUAGE_KEY] ?: AppLanguage.ENGLISH.name }
            .first()

        return AppLanguage.entries.firstOrNull { it.name == value }
            ?: AppLanguage.ENGLISH
    }

    suspend fun setAutoFillCopiedUrl(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_FILL_COPIED_URL] = enabled
        }
    }
}
