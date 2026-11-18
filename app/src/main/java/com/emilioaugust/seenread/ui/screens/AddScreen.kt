package com.emilioaugust.seenread.ui.screens

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.datastore.SettingsDataStore
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.ui.enums.AddBookmarkForm
import com.emilioaugust.seenread.ui.enums.Intention
import com.emilioaugust.seenread.ui.enums.toBookmarkEntity
import com.emilioaugust.seenread.utils.normalizeUrl
import kotlinx.coroutines.flow.first

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(bookmarkViewModel: BookmarkViewModel, onBack: () -> Unit) {
    var form by remember { mutableStateOf(AddBookmarkForm()) }
    val context = LocalContext.current
    val currentUrl by rememberUpdatedState(form.url)

    LaunchedEffect(Unit) {
        val settings = SettingsDataStore(context)

        val autoFillEnabled = settings.autoFillCopiedUrlFlow.first()

        if (!autoFillEnabled || currentUrl.isNotBlank()) {
            return@LaunchedEffect
        }

        val clipboardManager =
            context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        val clip = clipboardManager.primaryClip ?: return@LaunchedEffect

        if (clip.itemCount == 0) return@LaunchedEffect

        val copiedText = clip.getItemAt(0)
            .coerceToText(context)
            ?.toString()
            ?: return@LaunchedEffect

        val url = normalizeUrl(copiedText) ?: return@LaunchedEffect

        form = form.copy(url = url)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Close, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.clickable { onBack() })

                        Spacer(Modifier.width(20.dp))

                        Text(
                            text = stringResource(R.string.save_bookmark),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            bookmarkViewModel.addBookmark(
                                bookmarkEntity = form.toBookmarkEntity(),
                                tags = form.tags
                            )
                            onBack()
                                  },
                        colors = ButtonDefaults.textButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White)
                    ) {
                        Text(stringResource(R.string.save))
                    }
                    Spacer(Modifier.width(16.dp))
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
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            OutlinedTextField(
                value = form.url,
                onValueChange = {
                    if (currentUrl.isBlank()) {
                    form = form.copy(url = form.url)
                } },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 8.dp),
                placeholder = {
                    Text(
                        "https://github.com",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
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
                )
            )

            Spacer(Modifier.height(16.dp))

            form.title?.let {
                OutlinedTextField(
                    value = it,
                    onValueChange = { form = form.copy(title = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 8.dp),
                    placeholder = {
                        Text(
                            stringResource(R.string.title),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
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
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(32.dp))

            Text(stringResource(R.string.note_optional), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            form.note?.let { it ->
                OutlinedTextField(
                    value = it,
                    onValueChange = { form = form.copy(note = it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(120.dp)
                        .padding(end = 8.dp),
                    placeholder = {
                        Text(
                            stringResource(R.string.write_your_note_about_bookmark),
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    shape = MaterialTheme.shapes.medium,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.5f
                        ),
                        unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                            alpha = 0.5f
                        ),
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    maxLines = 4
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(stringResource(R.string.intention), style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Intention.entries.forEach { intention ->
                    FilterChip(
                        selected = form.intention == intention,
                        onClick = {
                            form = form.copy(intention = intention)
                        },
                        leadingIcon = {
                            Icon(painterResource(
                                if(form.intention == intention) intention.selectedIcon
                                else intention.icon
                            ), contentDescription = null,
                                modifier = Modifier.size(24.dp), tint = Color.Unspecified)
                        },
                        label = {
                            Text(
                                text = stringResource(intention.title),
                                style = MaterialTheme.typography.bodyMedium
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

            Spacer(Modifier.height(24.dp))

            TagsInput(
                tags = form.tags,
                onTagsChange = { newTags ->
                    form = form.copy(tags = newTags)
                }
            )
        }
    }
}

@Composable
fun TagsInput(
    tags: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }

    fun addTag() {
        val newTag = tagInput.trim()

        if (
            newTag.isNotBlank() &&
            tags.none { it.equals(newTag, ignoreCase = true) }
        ) {
            onTagsChange(tags + newTag)
        }

        tagInput = ""
    }

    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.tags_optional),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = tagInput,
            onValueChange = { tagInput = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(stringResource(R.string.add_a_tag), style = MaterialTheme.typography.bodyMedium)
            },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { addTag() }
            ),
            trailingIcon = {
                IconButton(
                    onClick = { addTag() },
                    enabled = tagInput.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null
                    )
                }
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
                focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
        )

        if (tags.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = true,
                        onClick = {
                            onTagsChange(tags - tag)
                        },
                        label = {
                            Text(tag)
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }
        }
    }
}