package com.emilioaugust.seenread.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.ui.screens.AboutScreen
import com.emilioaugust.seenread.ui.screens.NavScreen
import com.emilioaugust.seenread.ui.screens.WelcomeScreen

@Composable
fun AppRoot(
    viewModel: BookmarkViewModel,
    settingsViewModel: SettingsViewModel,
) {
    val isFirstLaunch by settingsViewModel.isFirstLaunch.collectAsStateWithLifecycle()
    var showAbout by rememberSaveable { mutableStateOf(false) }

    when (isFirstLaunch) {
        null -> Box(Modifier.fillMaxSize())

        true -> {
            WelcomeScreen(
                onNavigateTo = { settingsViewModel.setFirstLaunchCompleted() },
                onNavigateToAbout = { showAbout = true }
            )

            if (showAbout) {
                AboutDialog(onDismiss = { showAbout = false })
            }
        }

        false -> NavScreen(viewModel, settingsViewModel)
    }
}

@Composable
fun AboutDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AboutScreen(onBack = onDismiss)
        }
    }
}