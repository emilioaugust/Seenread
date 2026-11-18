package com.emilioaugust.seenread.ui.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import com.emilioaugust.seenread.ui.commons.TextButtonCard
import com.emilioaugust.seenread.ui.enums.Intention
import com.emilioaugust.seenread.ui.screens.TagsInput

@Composable
fun EditSheet(bookmark: BookmarkWithTags,
              onDismiss: () -> Unit,
              onOpen: (String) -> Unit,
              onSave: (BookmarkEntity, List<String>) -> Unit
) {
    var title by remember(bookmark.bookmark.id) {
        mutableStateOf(bookmark.bookmark.title.orEmpty())
    }

    var url by remember(bookmark.bookmark.id) {
        mutableStateOf(bookmark.bookmark.url)
    }

    var note by remember(bookmark.bookmark.id) {
        mutableStateOf(bookmark.bookmark.note.orEmpty())
    }

    var intention by remember(bookmark.bookmark.id) {
        mutableStateOf(bookmark.bookmark.intention)
    }

    var tags by remember(bookmark.bookmark.id) {
        mutableStateOf(bookmark.tags.map { it.name })
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)) {
        Text(
            stringResource(R.string.edit_details), style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer)

        Spacer(Modifier.height(24.dp))

        Text(stringResource(R.string.title), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                ),
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                ),
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
        )

        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.url), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(4.dp))
        OutlinedTextField(
            value = url,
            onValueChange = { url = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                ),
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                ),
                unfocusedPlaceholderColor = Color.Gray,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            trailingIcon = {
                IconButton(
                    onClick = { onOpen(url) },
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
        )

        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.note_optional),
            style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(4.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(120.dp)
                .padding(end = 8.dp),
            shape = MaterialTheme.shapes.medium,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                ),
                unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                    alpha = 0.5f
                ),
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedPlaceholderColor = Color.Gray,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            maxLines = 4
        )

        Spacer(Modifier.height(16.dp))

        Text(stringResource(R.string.intention), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Intention.entries.forEach { inten ->
                FilterChip(
                    selected = intention == inten,
                    onClick = {
                        intention = inten
                    },
                    label = {
                        Text(
                            text = stringResource(inten.title),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor =  MaterialTheme.colorScheme.onPrimaryContainer,
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    shape = MaterialTheme.shapes.medium
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        TagsInput(
            tags = tags,
            onTagsChange = { newTags ->
                tags = newTags
            }
        )

        Spacer(Modifier.height(16.dp))

        TextButtonCard(
            text = stringResource(R.string.save),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            onClick = {
                val updatedBookmark = bookmark.bookmark.copy(
                    title = title.trim().ifBlank { null },
                    url = url.trim(),
                    note = note.trim().ifBlank { null },
                    intention = intention,
                    faviconUrl = if (url.trim() == bookmark.bookmark.url) {
                        bookmark.bookmark.faviconUrl
                    } else {
                        null
                    },
                    updatedAt = System.currentTimeMillis()
                )

                onSave(updatedBookmark, tags)
            }
        )
    }
}