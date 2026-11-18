package com.emilioaugust.seenread.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.ui.commons.BottomBarNavigation
import com.emilioaugust.seenread.ui.commons.DeleteAlertDialog
import com.emilioaugust.seenread.ui.commons.TopBarMenu
import com.emilioaugust.seenread.ui.enums.Intention
import com.emilioaugust.seenread.ui.enums.Screen
import com.emilioaugust.seenread.ui.sheets.EditSheet
import com.emilioaugust.seenread.utils.openExternalBrowser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(navController: NavController,
                  viewModel: BookmarkViewModel, settingsViewModel: SettingsViewModel,
                  onNavigateToDetails: (String) -> Unit) {
    val context = LocalContext.current
    val selectedIntention by viewModel.selectedIntention.collectAsStateWithLifecycle()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val bookmarkToEdit by viewModel.bookmarkToEdit.collectAsStateWithLifecycle()
    val openInExternalBrowser by settingsViewModel.openInExternalBrowser.collectAsStateWithLifecycle()

    var openAlertDialog by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkEntity?>(null) }

    val libraryItems by viewModel.libraryItems.collectAsStateWithLifecycle()
    val filteredItems = remember(libraryItems, searchQuery) {
        libraryItems.filter { item ->
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.library),
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

            Spacer(Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
            ) {
                items(Intention.entries, key = { it.name }) { intention ->

                    val isSelected = selectedIntention == intention

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.primaryContainer
                        },
                        animationSpec = tween(durationMillis = 250),
                        label = "chipContainerColor"
                    )

                    val labelColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            Color.White
                        } else {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        animationSpec = tween(durationMillis = 250),
                        label = "chipLabelColor"
                    )

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1f,
                        animationSpec = tween(durationMillis = 200),
                        label = "chipScale"
                    )

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            viewModel.onIntentionSelected(intention)
                        },
                        leadingIcon = {
                            Icon(painterResource(
                                if(isSelected) intention.selectedIcon
                                    else intention.icon
                            ), contentDescription = null,
                                modifier = Modifier.size(18.dp), tint = Color.Unspecified)
                        },
                        label = {
                            Text(
                                text = stringResource(intention.title),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        modifier = Modifier
                            .heightIn(min = 36.dp)
                            .scale(scale),
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = containerColor,
                            labelColor = labelColor,
                            selectedContainerColor = containerColor,
                            selectedLabelColor = labelColor
                        ),
                        shape = MaterialTheme.shapes.extraLarge
                    )
                }
            }

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
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.bookmark.id }
                    ) { item ->
                        BookmarkCard(
                            bookmarkEntity = item.bookmark,
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    item.bookmark.url.toUri()
                                )
                                context.startActivity(intent)

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