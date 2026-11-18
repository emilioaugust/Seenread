package com.emilioaugust.seenread.ui.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.ui.enums.Screen
import com.emilioaugust.seenread.utils.formatSavedTime
import com.emilioaugust.seenread.utils.getClearDomain
import com.emilioaugust.seenread.utils.getFaviconUrl
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.ui.commons.BottomBarNavigation
import com.emilioaugust.seenread.ui.commons.DeleteAlertDialog
import com.emilioaugust.seenread.ui.commons.MoreOnCard
import com.emilioaugust.seenread.ui.commons.TopBarMenu
import com.emilioaugust.seenread.ui.sheets.EditSheet
import com.emilioaugust.seenread.utils.openExternalBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentScreen(navController: NavController,
                 viewModel: BookmarkViewModel,
                 settingsViewModel: SettingsViewModel,
                 onNavigateToDetails: (String) -> Unit) {
    val context = LocalContext.current

    var searchQuery by rememberSaveable { mutableStateOf("") }

    var openAlertDialog by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkEntity?>(null) }

    val bookmarkItems by viewModel.items.collectAsState()
    val bookmarkToEdit by viewModel.bookmarkToEdit.collectAsStateWithLifecycle()

    val filteredItems = remember(bookmarkItems, searchQuery) {
        bookmarkItems.filter { item ->
            val query = searchQuery.trim()

            query.isBlank() ||
                    item.bookmark.title?.contains(
                        query,
                        ignoreCase = true
                    ) == true ||

                    item.bookmark.url.contains(
                        query,
                        ignoreCase = true
                    ) ||

                    item.tags.any { tag ->
                        tag.name.contains(
                            query,
                            ignoreCase = true
                        )
                    }
        }
    }

    val openInExternalBrowser by settingsViewModel.openInExternalBrowser.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.recent),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    TopBarMenu(
                        onArchiveClick = {
                            navController.navigate(Screen.Archive.route)
                        },
                        onFavoritesClick = {
                            navController.navigate(Screen.Favorite.route)
                        },
                        onSettingsClick = {
                            navController.navigate(Screen.Settings.route)
                        }
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            BottomBarNavigation(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.Add.route)
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.offset(y = (-16).dp)
            ) {

                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Import image"
                )

            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 16.dp
                    ),
                placeholder = {
                    Text(stringResource(R.string.search))
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.5f
                    ),
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                        alpha = 0.5f
                    ),
                    unfocusedPlaceholderColor = Color.Gray,
                    focusedPlaceholderColor = Color.Gray,
                    unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                singleLine = true,
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = Color.Gray
                    )
                },
                shape = MaterialTheme.shapes.extraLarge
            )

            if(filteredItems.isEmpty()) {
                if(searchQuery.isNotBlank()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.nothing_found),
                            contentDescription = null,
                            modifier = Modifier.size(280.dp),
                            contentScale = ContentScale.Fit
                        )

                        Text(
                            text = stringResource(R.string.nothing_found),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(
                                R.string.no_bookmarks_match_your_search_for,
                                searchQuery
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(R.drawable.no_bookmarks),
                            contentDescription = null,
                            modifier = Modifier.size(320.dp),
                            contentScale = ContentScale.Fit
                        )

                        Text(
                            text = stringResource(R.string.no_bookmarks_yet),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(
                            text = stringResource(R.string.save_links_from_anywhere_using_the_share_menu_or_the_button),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.bookmark.id }
                    ) { item ->
                        BookmarkCard(
                            bookmarkEntity = item.bookmark,
                            onClick = {
                                if(openInExternalBrowser) {
                                    openExternalBrowser(context, item.bookmark.url)
                                } else {
                                    val encodedUrl = Uri.encode(item.bookmark.url)
                                    navController.navigate("browser/$encodedUrl")
                                }
                                viewModel.markAsOpened(item.bookmark.id)
                            },
                            onLongClick = { onNavigateToDetails(item.bookmark.id) },
                            onEdit = { viewModel.startEditing(item.bookmark.id) },
                            onFavorite = {
                                viewModel.setFavorite(
                                    item.bookmark.id,
                                    !item.bookmark.isFavorite,
                                    System.currentTimeMillis()
                                )
                            },
                            onArchive = {
                                viewModel.setArchived(
                                    id = item.bookmark.id,
                                    isArchived = !item.bookmark.isArchived,
                                    updatedAt = System.currentTimeMillis()
                                )
                            },
                            onDelete = {
                                bookmarkToDelete = item.bookmark
                                openAlertDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (bookmarkToEdit != null) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.stopEditing()
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            dragHandle = {}
        ) {
            EditSheet(
                bookmark = bookmarkToEdit!!,
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

    if(openAlertDialog && bookmarkToDelete != null) {
        DeleteAlertDialog(
            onDismissRequest = { openAlertDialog = false },
            onConfirmation = {
                viewModel.deleteBookmark(bookmarkToDelete!!.id)
                openAlertDialog = false
            },
            dialogTitle = stringResource(R.string.are_you_sure),
            dialogText = stringResource(R.string.do_you_really_want_to_delete_this),
            icon = Icons.Default.DeleteForever
        )
    }
}

@Composable
fun BookmarkCard(bookmarkEntity: BookmarkEntity, onClick: () -> Unit, onLongClick: () -> Unit,
                 onEdit: () -> Unit,
                 onFavorite: () -> Unit,
                 onArchive: () -> Unit,
                 onDelete: () -> Unit) {
    val context = LocalContext.current
    var moreMenuExpanded by remember(bookmarkEntity.id) {
        mutableStateOf(false)
    }
    val faviconUrl = bookmarkEntity.faviconUrl
        ?: getFaviconUrl(bookmarkEntity.url)

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(12.dp)
            .combinedClickable(
                onClick = { onClick() },
                onLongClick = { onLongClick() }
            )
    ) {
        if (faviconUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(faviconUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Favicon",
                modifier = Modifier.size(26.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Website",
                modifier = Modifier.size(26.dp)
            )
        }

        Column {
            Text(
                text = bookmarkEntity.title
                    ?.takeIf { it.isNotBlank() }
                    ?: bookmarkEntity.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.width(250.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = getClearDomain(bookmarkEntity.url) ?: bookmarkEntity.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodySmall,
            )

            Spacer(Modifier.height(4.dp))

            Row {
                Text(
                    text = formatSavedTime(bookmarkEntity.createdAt),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = "•",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(Modifier.width(8.dp))

                Text(
                    text = stringResource(bookmarkEntity.intention.title),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Box {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier.clickable { moreMenuExpanded = true }
            )

            MoreOnCard(
                expanded = moreMenuExpanded,
                onDismiss = { moreMenuExpanded = false },
                onEdit = {
                    moreMenuExpanded = false
                    onEdit()
                },
                onFavorite = {
                    moreMenuExpanded = false
                    onFavorite()
                },
                onArchive = {
                    moreMenuExpanded = false
                    onArchive()
                },
                onDelete = {
                    moreMenuExpanded = false
                    onDelete()
                },
                isFavorite = bookmarkEntity.isFavorite,
                isArchived = bookmarkEntity.isArchived
            )
        }
    }
}