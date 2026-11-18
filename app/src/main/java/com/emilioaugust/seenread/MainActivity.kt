package com.emilioaugust.seenread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.core.os.LocaleListCompat
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

    private val sharedUrl = mutableStateOf<String?>(null)

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
        handleShareIntent(intent)
        super.onCreate(savedInstanceState)
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
                AppRoot(viewModel, settingsViewModel,
                    sharedUrl = sharedUrl.value,
                    onSharedUrlConsumed = {
                        sharedUrl.value = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        handleShareIntent(intent)
    }

    private fun handleShareIntent(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) return
        if (intent.type != "text/plain") return

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: return

        val url = extractUrl(sharedText) ?: return

        sharedUrl.value = url

        Toast.makeText(
            this,
            "Bookmark successfully saved",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun extractUrl(text: String): String? {
        val matcher = Patterns.WEB_URL.matcher(text)

        if (!matcher.find()) return null

        return matcher.group()
            ?.trim()
            ?.trimEnd('.', ',', '!', '?', ')', ']')
    }
}
