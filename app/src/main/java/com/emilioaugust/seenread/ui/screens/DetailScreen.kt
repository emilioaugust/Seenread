package com.emilioaugust.seenread.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.ui.commons.DeleteAlertDialog
import com.emilioaugust.seenread.ui.sheets.EditSheet
import com.emilioaugust.seenread.utils.formatLastOpened
import com.emilioaugust.seenread.utils.formatSavedTime
import com.emilioaugust.seenread.utils.openExternalBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(bookmarkId: String, viewModel: BookmarkViewModel,
                 settingsViewModel: SettingsViewModel,
                 navController: NavController,
                 onBack: () -> Unit) {
    val context = LocalContext.current

    val bookmark by viewModel.getBookmarkById(bookmarkId).collectAsState(initial = null)
    val bookmarkWithTags by viewModel.getBookmarkWithTags(bookmarkId).collectAsState(initial = null)
    var openAlertDialog by remember { mutableStateOf(false) }
    val bookmarkToEdit by viewModel.bookmarkToEdit.collectAsStateWithLifecycle()

    if (bookmark == null) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            return
        }
    }

    val currentBookmark = bookmark ?: return
    val openInExternalBrowser by settingsViewModel.openInExternalBrowser.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.KeyboardBackspace, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.clickable { onBack() })

                        Spacer(Modifier.width(20.dp))

                        currentBookmark.title?.let {
                            Text(
                                text = it,
                                color = MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.headlineSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.width(350.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            currentBookmark.title?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall,

                )
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (currentBookmark.faviconUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(currentBookmark.faviconUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Favicon",
                        modifier = Modifier.size(24.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Website",
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                Text(
                    text = currentBookmark.url,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.shapes.large
                )
                .padding(horizontal = 24.dp, vertical = 10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(currentBookmark.intention.icon), contentDescription = null,
                        modifier = Modifier.size(24.dp), tint = Color.Unspecified
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        stringResource(currentBookmark.intention.title),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row {
                Text(
                    text = formatSavedTime(currentBookmark.createdAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = "|",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    text = formatLastOpened(currentBookmark.lastOpenedAt),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.note), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            if(currentBookmark.note == "" || currentBookmark.note == null) {
                Text(stringResource(R.string.there_s_no_note),
                    style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            } else {
                currentBookmark.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = Color.Gray) }
            }

            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.tags), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(12.dp))

            bookmarkWithTags?.let { bookmark ->
                if (bookmark.tags.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bookmark.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant,
                                        MaterialTheme.shapes.large
                                    )
                                    .padding(
                                        horizontal = 16.dp,
                                        vertical = 10.dp
                                    )
                            ) {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            HorizontalDivider(thickness = 1.dp, color = Color.LightGray)

            Spacer(Modifier.height(20.dp))

            ButtonRow(icon = Icons.Default.OpenInNew, text = stringResource(R.string.open), onClick = {
                val intent = Intent(
                    Intent.ACTION_VIEW,
                    currentBookmark.url.toUri()
                )
                context.startActivity(intent)
                viewModel.markAsOpened(currentBookmark.id)
            })
            Spacer(Modifier.height(20.dp))
            ButtonRow(
                icon = Icons.Default.Edit,
                text = stringResource(R.string.edit),
                onClick = {
                    viewModel.startEditing(currentBookmark.id)
                }
            )
            Spacer(Modifier.height(20.dp))
            ButtonRow(
                icon = if(!currentBookmark.isFavorite) {
                    Icons.Default.FavoriteBorder
                } else {
                    Icons.Default.Favorite
                },
                text = if(!currentBookmark.isFavorite) {
                    stringResource(R.string.add_to_favorites)
                } else {
                    stringResource(R.string.remove_from_favorites)
                },
                onClick = {
                    viewModel.setFavorite(
                        currentBookmark.id,
                        !currentBookmark.isFavorite,
                        System.currentTimeMillis()
                    )
                }
            )

            Spacer(Modifier.height(20.dp))
            ButtonRow(
                icon = if(currentBookmark.isArchived) {
                    Icons.Default.Unarchive
                } else {
                    Icons.Default.Archive
                },
                text = if(currentBookmark.isArchived) {
                    stringResource(R.string.remove_from_archives)
                } else {
                    stringResource(R.string.archive)
                },
                onClick = {
                    viewModel.setArchived(
                        id = currentBookmark.id,
                        isArchived = !currentBookmark.isArchived,
                        updatedAt = System.currentTimeMillis()
                    )
                }
            )
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    openAlertDialog = true
                }
            ) {
                Icon(imageVector = Icons.Outlined.Delete, contentDescription = null, tint = Color(0xFFF72F35))
                Spacer(Modifier.width(16.dp))
                Text(
                    stringResource(R.string.delete), style = MaterialTheme.typography.bodyLarge, color = Color(0xFFF72F35),
                    fontWeight = FontWeight.Bold)
            }
        }
    }

    bookmarkToEdit?.let { bookmark ->
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.stopEditing()
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            dragHandle = {}
        ) {
            EditSheet(
                bookmark = bookmark,
                onDismiss = {
                    viewModel.stopEditing()
                },
                onOpen = { url ->
                    if(openInExternalBrowser) {
                        openExternalBrowser(context, url)
                    } else {
                        val encodedUrl = Uri.encode(url)
                        navController.navigate("browser/$encodedUrl")
                    }
                },
                onSave = { updatedBookmark, updatedTags ->
                    viewModel.updateBookmarkWithTags(
                        bookmark = updatedBookmark,
                        tags = updatedTags
                    )
                    viewModel.stopEditing()
                }
            )
        }
    }

    if(openAlertDialog) {
        DeleteAlertDialog(
            onDismissRequest = { openAlertDialog = false },
            onConfirmation = {
                viewModel.deleteBookmark(currentBookmark.id)
                openAlertDialog = false
                navController.popBackStack()
            },
            dialogTitle = stringResource(R.string.are_you_sure),
            dialogText = stringResource(R.string.do_you_really_want_to_delete_this),
            icon = Icons.Default.DeleteForever
        )
    }
}

@Composable
fun ButtonRow(icon: ImageVector, text: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onClick() }) {
        Icon(imageVector = icon, contentDescription = null)
        Spacer(Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}