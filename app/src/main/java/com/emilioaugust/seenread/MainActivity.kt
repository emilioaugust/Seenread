package com.emilioaugust.seenread

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.emilioaugust.seenread.data.datastore.SettingsDataStore
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModelFactory
import com.emilioaugust.seenread.ui.AppRoot
import com.emilioaugust.seenread.ui.theme.BookieTheme
import com.emilioaugust.seenread.ui.theme.ThemeMode
import com.emilioaugust.seenread.utils.LocaleHelper
import kotlinx.coroutines.runBlocking
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val viewModel: BookmarkViewModel by viewModels()

    private val settingsViewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(
            SettingsDataStore(applicationContext)
        )
    }

    private lateinit var settingsDataStore: SettingsDataStore

    override fun attachBaseContext(newBase: Context) {
        val context = runBlocking {
            val dataStore = SettingsDataStore(newBase)
            val lang = dataStore.getAppLanguage()
            LocaleHelper.setLocale(newBase, lang.code)
        }
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        settingsDataStore = SettingsDataStore(applicationContext)
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()
        setContent {
            val themeMode by settingsViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val darkTheme = when(themeMode) {
                ThemeMode.DARK -> true
                ThemeMode.LIGHT -> false
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }
            val language by settingsViewModel.language.collectAsState()

            LaunchedEffect(language) {
                val locales = LocaleListCompat.forLanguageTags(language.code)
                AppCompatDelegate.setApplicationLocales(locales)
            }

            BookieTheme(darkTheme = darkTheme) {
                AppRoot(viewModel, settingsViewModel)
            }
        }
    }
}
