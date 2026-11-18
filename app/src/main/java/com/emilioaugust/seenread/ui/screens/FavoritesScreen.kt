package com.emilioaugust.seenread.ui.screens

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.ui.commons.DeleteAlertDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(viewModel: BookmarkViewModel, onBack: () -> Unit,
                    onNavigateToDetails: (String) -> Unit) {
    val context = LocalContext.current
    val favoriteItems by viewModel.favoriteItems.collectAsState()
    var openAlertDialog by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkEntity?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.KeyboardBackspace, contentDescription = null,
                            tint =  MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.clickable { onBack() })

                        Spacer(Modifier.width(20.dp))
                        Text(
                            text = stringResource(R.string.favorites),
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
        Column(modifier = Modifier.padding(padding)) {
            if(favoriteItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.no_favorites),
                        contentDescription = null,
                        modifier = Modifier.size(320.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = stringResource(R.string.no_favorite_items_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.mark_important_items_as_favorites_for_quick_access),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 8.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = favoriteItems,
                        key = { it.id }
                    ) { item ->
                        BookmarkCard(
                            bookmarkEntity = item,
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    item.url.toUri()
                                )
                                context.startActivity(intent)
                                viewModel.markAsOpened(item.id)
                            },
                            onLongClick = { onNavigateToDetails(item.id) },
                            onEdit = { viewModel.startEditing(item.id) },
                            onFavorite = {
                                viewModel.setFavorite(
                                    item.id,
                                    !item.isFavorite,
                                    System.currentTimeMillis()
                                )
                            },
                            onArchive = {
                                viewModel.setArchived(
                                    id = item.id,
                                    isArchived = !item.isArchived,
                                    updatedAt = System.currentTimeMillis()
                                )
                            },
                            onDelete = {
                                bookmarkToDelete = item
                                openAlertDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if(openAlertDialog) {
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