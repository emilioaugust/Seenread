package com.emilioaugust.seenread.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
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
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.ui.commons.BottomBarNavigation
import com.emilioaugust.seenread.utils.formatSavedTime
import com.emilioaugust.seenread.utils.getClearDomain
import com.emilioaugust.seenread.utils.getFaviconUrl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(navController: NavController, viewModel: BookmarkViewModel) {
    val context = LocalContext.current
    val bookmarksOlderThan14Days by viewModel.bookmarksOlderThan14Days.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.review),
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
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
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                stringResource(R.string.take_a_look_at_older_bookmarks_keep_what_s_useful_archive_or_delete_what_you_don_t_need),
                style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(16.dp))

            if(bookmarksOlderThan14Days.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.no_review),
                        contentDescription = null,
                        modifier = Modifier.size(320.dp),
                        contentScale = ContentScale.Fit
                    )

                    Text(
                        text = stringResource(R.string.no_bookmarks_to_review),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.you_ll_see_older_bookmarks_here_when_it_s_time_to_revisit_them),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = bookmarksOlderThan14Days,
                    key = { it.id }
                ) { item ->
                    ReviewCard(
                        context = context,
                        bookmarkEntity = item,
                        onKeep = { viewModel.keepFromReview(item) },
                        onArchive = {
                            viewModel.setArchived(
                                id = item.id,
                                isArchived = !item.isArchived,
                                updatedAt = System.currentTimeMillis()
                            ) },
                        onDelete = { viewModel.deleteBookmark(item.id) }
                    )
                }
            }
        }
    }
}


@Composable
fun ReviewCard(
    context: Context,
    bookmarkEntity: BookmarkEntity,
    onKeep: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedState by remember(bookmarkEntity.id) {
        mutableStateOf(false)
    }
    var feedback by remember(bookmarkEntity.id) {
        mutableStateOf<ReviewAction?>(null)
    }

    val scope = rememberCoroutineScope()

    fun handleAction(action: ReviewAction, callback: () -> Unit) {
        if (feedback != null) return

        feedback = action
        scope.launch {
            delay(2_000)
            callback()
        }
    }

    val faviconUrl = bookmarkEntity.faviconUrl
        ?: getFaviconUrl(bookmarkEntity.url)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable {
                if (feedback == null) {
                    expandedState = !expandedState
                }
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
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

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = bookmarkEntity.title
                                ?.takeIf { it.isNotBlank() }
                                ?: bookmarkEntity.url,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = getClearDomain(bookmarkEntity.url)
                                ?: bookmarkEntity.url,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(Modifier.height(4.dp))

                        Text(
                            text = formatSavedTime(context, bookmarkEntity.createdAt),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }

                AnimatedVisibility(
                    visible = expandedState,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = {
                                    handleAction(ReviewAction.Keep, onKeep)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    contentColor = Color(0xFF07831D)
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.keep))
                            }

                            TextButton(
                                onClick = {
                                    handleAction(ReviewAction.Archive, onArchive)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onSurface,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.archive))
                            }

                            TextButton(
                                onClick = {
                                    handleAction(ReviewAction.Delete, onDelete)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onError
                                ),
                                shape = MaterialTheme.shapes.medium
                            ) {
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                }
            }

            feedback?.let { action ->
                val backgroundColor = when (action) {
                    ReviewAction.Keep -> Color(0xFF2E7D32)
                    ReviewAction.Archive -> Color.Gray
                    ReviewAction.Delete -> Color(0xFFC62828)
                }

                val icon = when (action) {
                    ReviewAction.Keep -> Icons.Default.Check
                    ReviewAction.Archive -> Icons.Default.Archive
                    ReviewAction.Delete -> Icons.Default.Delete
                }

                val description = when (action) {
                    ReviewAction.Keep -> stringResource(R.string.saved)
                    ReviewAction.Archive -> stringResource(R.string.in_archived)
                    ReviewAction.Delete -> stringResource(R.string.deleted)
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(MaterialTheme.shapes.medium)
                        .background(backgroundColor.copy(alpha = 0.92f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = description,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }
    }
}

private enum class ReviewAction {
    Keep, Archive, Delete
}

