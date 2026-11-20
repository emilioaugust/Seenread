package com.emilioaugust.seenread.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.ui.commons.DeleteAlertDialog
import com.emilioaugust.seenread.ui.enums.AppLanguage
import com.emilioaugust.seenread.ui.enums.Screen
import com.emilioaugust.seenread.ui.theme.ThemeMode
import com.emilioaugust.seenread.ui.theme.lighten
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel,
                   viewModel: BookmarkViewModel, onBack: () -> Unit) {
    val themeMode by settingsViewModel.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
    val openInExternalBrowser by settingsViewModel.openInExternalBrowser.collectAsStateWithLifecycle()
    val autoFillCopiedUrlFlow by settingsViewModel.autoFillCopiedUrlFlow.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var openAlertDialogDeleteAll by remember { mutableStateOf(false) }
    var openAlertDialogDeleteAllArchived by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }

    val language by settingsViewModel.language.collectAsState(initial = AppLanguage.ENGLISH)
    val activity = LocalContext.current as Activity

    // launchers

    val exportJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val content = viewModel.exportBookmarksJson()

                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                        it.write(content)
                    } ?: error("Couldn't create a file")
                }

                snackbarHostState.showSnackbar("JSON successfully exported")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Export error: ${e.localizedMessage ?: "unknown error"}"
                )
            }
        }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val content = viewModel.exportBookmarksCsv()

                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use {
                        it.write(content)
                    } ?: error("Couldn't create a file")
                }

                snackbarHostState.showSnackbar("CSV successfully exported")
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Export error: ${e.localizedMessage ?: "unknown error"}"
                )
            }
        }
    }

    val importJsonLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText()
                    } ?: error("Couldn't read a file")
                }

                val result = viewModel.importBookmarksJson(content)

                snackbarHostState.showSnackbar(
                    "Imported: ${result.imported}, skipped: ${result.skipped}"
                )
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Import error: ${e.localizedMessage ?: "unknown error"}"
                )
            }
        }
    }

    val importCsvLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.bufferedReader()?.use {
                        it.readText()
                    } ?: error("Couldn't read a file")
                }

                val result = viewModel.importBookmarksCsv(content)

                snackbarHostState.showSnackbar(
                    context.getString(R.string.imported_skipped, result.imported, result.skipped)
                )
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Import error: ${e.localizedMessage ?: "unknown error"}"
                )
            }
        }
    }

    val exportHtmlLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val content = viewModel.exportBookmarksHtml()

                withContext(Dispatchers.IO) {
                    context.contentResolver
                        .openOutputStream(uri)
                        ?.bufferedWriter(Charsets.UTF_8)
                        ?.use { it.write(content) }
                        ?: error("Couldn't create a file")
                }

                snackbarHostState.showSnackbar(context.getString(R.string.html_successfully_exported))
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Export error: ${e.localizedMessage ?: "Unknown error"}"
                )
            }
        }
    }

    val importHtmlLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri ?: return@rememberLauncherForActivityResult

        scope.launch {
            try {
                val content = withContext(Dispatchers.IO) {
                    context.contentResolver
                        .openInputStream(uri)
                        ?.bufferedReader(Charsets.UTF_8)
                        ?.use { it.readText() }
                        ?: error("Couldn't read a file")
                }

                val result = viewModel.importBookmarksHtml(content)

                snackbarHostState.showSnackbar(
                    context.getString(R.string.imported_skipped, result.imported, result.skipped)
                )
            } catch (e: Exception) {
                snackbarHostState.showSnackbar(
                    "Import error: ${e.localizedMessage ?: "unknown error"}"
                )
            }
        }
    }

    // end of launchers

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.KeyboardBackspace, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.clickable { onBack() })

                        Spacer(Modifier.width(20.dp))
                        Text(
                            text = stringResource(R.string.settings),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
    ) { padding ->
        LazyColumn(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(horizontal = 16.dp)) {
            item {
                Text(text = stringResource(R.string.appearance), style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    ThemeMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            selected = themeMode == mode,
                            onClick = { settingsViewModel.setThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = ThemeMode.entries.size
                                )
                        ) {
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> stringResource(R.string.system)
                                    ThemeMode.LIGHT -> stringResource(R.string.light)
                                    ThemeMode.DARK -> stringResource(R.string.dark)
                                },
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(text = stringResource(R.string.preferences), style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray)
                Spacer(Modifier.height(18.dp))
                SettingsSwitchItem(
                    icon = R.drawable.open_link,
                    text = stringResource(R.string.open_links),
                    subText = stringResource(R.string.open_links_in_an_external_browser),
                    checked = openInExternalBrowser,
                    onCheckedChange = {
                        settingsViewModel.setOpenInExternalBrowser(!openInExternalBrowser)
                    }
                )

                Spacer(Modifier.height(18.dp))

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(Modifier.height(18.dp))

                SettingsSwitchItem(
                    icon = R.drawable.auto_fill,
                    text = stringResource(R.string.auto_fill_links),
                    size = 24.dp,
                    subText = stringResource(R.string.fill_links_in_url_field_automatically),
                    checked = autoFillCopiedUrlFlow,
                    onCheckedChange = {
                        settingsViewModel.setAutoFillCopiedUrl(!autoFillCopiedUrlFlow)
                    }
                )

                Spacer(Modifier.height(18.dp))

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(Modifier.height(18.dp))

                SettingsDropdown(
                    icon = R.drawable.language,
                    text = stringResource(R.string.change_language),
                    subText = stringResource(R.string.change_app_language),
                    selected = language,
                    options = AppLanguage.entries,
                    label = { stringResource(it.titleRes) },
                    onSelected = { lang ->
                        scope.launch {
                            settingsViewModel.setLanguage(lang)
                            activity.recreate()
                        }
                    }
                )

                Spacer(Modifier.height(20.dp))

                Text(text = stringResource(R.string.data), style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray)
                Spacer(Modifier.height(18.dp))

                SettingsCard(
                    icon = R.drawable.export,
                    text = stringResource(R.string.export_bookmarks),
                    subText = stringResource(R.string.save_data_in_json_csv_or_html),
                    onClick = {
                        showExportDialog = true
                    }
                )

                Spacer(Modifier.height(18.dp))

                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)

                Spacer(Modifier.height(18.dp))

                SettingsCard(
                    icon = R.drawable.resource_import,
                    text = stringResource(R.string.import_bookmarks),
                    subText = stringResource(R.string.import_data_from_file),
                    onClick = {
                        showImportDialog = true
                    }
                )
                Spacer(Modifier.height(18.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                Spacer(Modifier.height(18.dp))

                SettingsCard(icon = R.drawable.archive_clear, text = stringResource(R.string.clear_archive), size = 24.dp,
                    subText = stringResource(R.string.delete_all_archived_items), onClick = { openAlertDialogDeleteAllArchived = true })

                Spacer(Modifier.height(18.dp))
                HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                Spacer(Modifier.height(18.dp))

                Row(modifier = Modifier.clickable { openAlertDialogDeleteAll = true },
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.delete), modifier = Modifier.size(24.dp),
                        contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.delete_all_bookmarks), style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(26.dp))

                Text(text = stringResource(R.string.about), style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray)
                Spacer(Modifier.height(18.dp))

                Row(modifier = Modifier.clickable { navController.navigate(Screen.About.route) },
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(R.drawable.about), modifier = Modifier.size(26.dp),
                        contentDescription = null, tint = Color.Unspecified)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.about), style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(Modifier.weight(1f))

                    Icon(
                        Icons.Default.KeyboardArrowRight,
                        contentDescription = null
                    )
                }

                Spacer(Modifier.height(20.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = {
                showExportDialog = false
            },
            title = {
                Text(stringResource(R.string.export_bookmarks), color = MaterialTheme.colorScheme.onPrimaryContainer)
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportHtmlLauncher.launch("seenread_bookmarks.html")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_as_html))
                    }

                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportJsonLauncher.launch("seenread_bookmarks.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_as_json))
                    }

                    TextButton(
                        onClick = {
                            showExportDialog = false
                            exportCsvLauncher.launch("seenread_bookmarks.csv")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_as_csv))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }

    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = {
                showImportDialog = false
            },
            title = {
                Text(stringResource(R.string.import_bookmarks), color = MaterialTheme.colorScheme.onPrimaryContainer)
            },
            text = {
                Column {
                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importHtmlLauncher.launch(
                                arrayOf(
                                    "text/html",
                                    "application/xhtml+xml",
                                    "text/*"
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_html))
                    }

                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importJsonLauncher.launch(
                                arrayOf("application/json", "text/json")
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_json))
                    }

                    TextButton(
                        onClick = {
                            showImportDialog = false
                            importCsvLauncher.launch(
                                arrayOf("text/csv", "text/comma-separated-values", "text/plain")
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.import_csv))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showImportDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    }

    if(openAlertDialogDeleteAll) {
        DeleteAlertDialog(
            onDismissRequest = { openAlertDialogDeleteAll = false },
            onConfirmation = {
                viewModel.deleteAllBookmarks()
                openAlertDialogDeleteAll = false
                navController.popBackStack()
            },
            dialogTitle = stringResource(R.string.are_you_sure),
            dialogText = stringResource(R.string.do_you_really_want_to_delete_all_bookmarks),
            icon = Icons.Default.DeleteForever
        )
    }

    if(openAlertDialogDeleteAllArchived) {
        DeleteAlertDialog(
            onDismissRequest = { openAlertDialogDeleteAllArchived = false },
            onConfirmation = {
                viewModel.deleteAllArchivedBookmarks()
                openAlertDialogDeleteAllArchived = false
                navController.popBackStack()
            },
            dialogTitle = stringResource(R.string.are_you_sure),
            dialogText = stringResource(R.string.do_you_really_want_to_delete_all_archived_bookmarks),
            icon = Icons.Default.DeleteForever
        )
    }
}

@Composable
fun SettingsCard(
    icon: Int,
    text: String,
    subText: String,
    size: Dp = 24.dp,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(icon),
            modifier = Modifier.size(size),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(text, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(4.dp))

            Text(
                subText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(Modifier.weight(1f))

        Icon(
            Icons.Default.KeyboardArrowRight,
            contentDescription = null
        )
    }
}

@Composable
fun <T> SettingsDropdown(
    icon: Int,
    text: String,
    subText: String,
    size: Dp = 24.dp,
    selected: T, options: List<T>, label: @Composable (T) -> String, onSelected: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(icon),
            modifier = Modifier.size(size),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(Modifier.width(12.dp))

        Column {
            Text(text, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(4.dp))

            Text(
                subText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Spacer(Modifier.weight(1f))

        var expanded by remember { mutableStateOf(false) }
        Box() {
            TextButton(
                onClick = { expanded = true },
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(
                    0.8.dp,
                    MaterialTheme.colorScheme.outline
                ),
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text(label(selected))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.padding(end = 16.dp)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(label(option), color = MaterialTheme.colorScheme.onPrimaryContainer) },
                        onClick = {
                            onSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSwitchItem(
    icon: Int,
    text: String,
    subText: String,
    size: Dp = 24.dp,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onCheckedChange(!checked)
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painterResource(icon),
            modifier = Modifier.size(size),
            contentDescription = null,
            tint = Color.Unspecified
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(Modifier.height(4.dp))

            Text(
                subText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary.lighten(0.8f),
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = Color.LightGray,
                uncheckedThumbColor = Color.Gray
            )
        )
    }
}