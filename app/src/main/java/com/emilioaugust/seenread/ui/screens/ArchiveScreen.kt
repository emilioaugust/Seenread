package com.emilioaugust.seenread.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardBackspace
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.ui.commons.MoreOnArchiveCard
import com.emilioaugust.seenread.utils.formatArchivedTime
import com.emilioaugust.seenread.utils.getClearDomain
import com.emilioaugust.seenread.utils.getFaviconUrl

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArchiveScreen(viewModel: BookmarkViewModel, onBack: () -> Unit,
                  onNavigateToDetails: (String) -> Unit) {
    val context = LocalContext.current
    val archivedItems by viewModel.archivedItems.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Filled.KeyboardBackspace, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.clickable { onBack() })

                        Spacer(Modifier.width(20.dp))
                        Text(
                            text = stringResource(R.string.archive),
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
            if(archivedItems.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.archive_empty),
                        contentDescription = null,
                        modifier = Modifier.size(320.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = stringResource(R.string.no_archived_items_yet),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.archive_items_you_no_longer_need_but_want_to_keep_for_later),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = archivedItems,
                        key = { it.id }
                    ) { item ->
                        ArchivedBookmarkCard(
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
                            onArchive = {
                                viewModel.setArchived(
                                    id = item.id,
                                    isArchived = !item.isArchived,
                                    updatedAt = System.currentTimeMillis()
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ArchivedBookmarkCard(bookmarkEntity: BookmarkEntity, onClick: () -> Unit, onLongClick: () -> Unit,
                         onArchive: () -> Unit) {
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
                modifier = Modifier.size(32.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Icon(
                imageVector = Icons.Default.Language,
                contentDescription = "Website",
                modifier = Modifier.size(32.dp)
            )
        }

        Column {
            Text(
                text = bookmarkEntity.title
                    ?.takeIf { it.isNotBlank() }
                    ?: bookmarkEntity.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(250.dp)
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = getClearDomain(bookmarkEntity.url) ?: bookmarkEntity.url,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = formatArchivedTime(context, bookmarkEntity.archivedAt),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        Spacer(Modifier.weight(1f))

        Box {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = null,
                modifier = Modifier.clickable { moreMenuExpanded = true }
            )

            MoreOnArchiveCard(
                expanded = moreMenuExpanded,
                onDismiss = { moreMenuExpanded = false },
                onArchive = { onArchive() },
                isArchived = bookmarkEntity.isArchived
            )
        }
    }
}