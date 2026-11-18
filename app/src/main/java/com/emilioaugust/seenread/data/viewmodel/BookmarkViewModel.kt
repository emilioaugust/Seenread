package com.emilioaugust.seenread.data.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.emilioaugust.seenread.data.database.AppDatabase
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import com.emilioaugust.seenread.data.repository.BookmarkRepository
import com.emilioaugust.seenread.data.repository.ImportResult
import com.emilioaugust.seenread.ui.enums.Intention
import com.emilioaugust.seenread.utils.PageMetadataFetcher
import com.emilioaugust.seenread.utils.normalizeUrl
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class BookmarkViewModel(application: Application) : AndroidViewModel(application) {
    private val repository =
        BookmarkRepository(AppDatabase.getInstance(application).bookmarkDao(),
            AppDatabase.getInstance(application).tagDao(),
            AppDatabase.getInstance(application)
        )

    val items = repository.getAllItems().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val archivedItems = repository.getAllArchived().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val favoriteItems = repository.getAllFavorites().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val bookmarksOlderThan14Days = repository.getBookmarksOlderThan14Days().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )



    private val _selectedIntention = MutableStateFlow<Intention?>(null)
    val selectedIntention = _selectedIntention.asStateFlow()

    private val _bookmarkToEditId = MutableStateFlow<String?>(null)
    val bookmarkToEditId = _bookmarkToEditId.asStateFlow()

    val bookmarkToEdit = bookmarkToEditId
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                repository.getBookmarkWithTags(id)
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    val libraryItems: StateFlow<List<BookmarkWithTags>> =
        selectedIntention
            .flatMapLatest { intention ->
                repository.getAllWithIntentions(intention)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    fun startEditing(bookmarkId: String) {
        _bookmarkToEditId.value = bookmarkId
    }

    fun stopEditing() {
        _bookmarkToEditId.value = null
    }

    fun onIntentionSelected(intention: Intention) {
        _selectedIntention.value =
            if (_selectedIntention.value == intention) {
                null
            } else {
                intention
            }
    }

    fun addBookmark(
        bookmarkEntity: BookmarkEntity,
        tags: List<String> = emptyList()
    ) {
        viewModelScope.launch {

            val normalizedUrl = normalizeUrl(bookmarkEntity.url)

            if (normalizedUrl == null) {
                return@launch
            }

            val normalizedBookmark = bookmarkEntity.copy(
                url = normalizedUrl
            )

            val userTitle = normalizedBookmark.title
                ?.trim()
                ?.takeIf { it.isNotEmpty() }

            val initialBookmark = normalizedBookmark.copy(
                title = userTitle ?: getFallbackTitle(normalizedUrl)
            )

            repository.insertBookmarkWithTags(
                bookmark = initialBookmark,
                tags = tags
            )

            launch {
                runCatching {
                    PageMetadataFetcher.fetchMetadata(normalizedUrl)
                }.onSuccess { metadata ->
                    metadata ?: return@onSuccess

                    val finalTitle = userTitle
                        ?: metadata.title
                        ?: getFallbackTitle(normalizedUrl)

                    val finalFaviconUrl = normalizedBookmark.faviconUrl
                        ?: metadata.faviconUrl

                    repository.updateBookmark(
                        initialBookmark.copy(
                            title = finalTitle,
                            faviconUrl = finalFaviconUrl
                        )
                    )
                }
            }
        }
    }

    fun addSharedLink(url: String) {
        val normalizedUrl = normalizeUrl(url) ?: return

        val now = System.currentTimeMillis()

        val bookmark = BookmarkEntity(
            id = UUID.randomUUID().toString(),
            url = normalizedUrl,
            title = null,
            coverImageUrl = null,
            faviconUrl = null,
            intention = Intention.READ,
            note = null,
            isFavorite = false,
            isArchived = false,
            createdAt = now,
            updatedAt = now,
            archivedAt = null,
            lastOpenedAt = null
        )

        addBookmark(bookmark)
    }


    fun keepFromReview(bookmark: BookmarkEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            repository.insertBookmarkWithTags(
                bookmark = bookmark.copy(createdAt = now, updatedAt = now),
                tags = emptyList()
            )
        }
    }

    fun setArchived(
        id: String,
        isArchived: Boolean,
        updatedAt: Long
    ) {
        viewModelScope.launch {
            repository.setArchived(id, isArchived, updatedAt)
        }
    }

    fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long) {
        viewModelScope.launch {
            repository.setFavorite(
                id = id,
                isFavorite = isFavorite,
                updatedAt = updatedAt
            )
        }
    }

    private fun getFallbackTitle(url: String): String {
        val normalizedUrl = if (
            url.startsWith("https://", true) ||
            url.startsWith("http://", true)
        ) {
            url
        } else {
            "https://$url"
        }

        return android.net.Uri.parse(normalizedUrl).host
            ?.removePrefix("www.")
            ?: url
    }

    fun getBookmarkById(id: String): Flow<BookmarkEntity?> {
        return repository.getBookmarkById(id)
    }

    fun getBookmarkByUrl(url: String): Flow<BookmarkEntity> {
        return repository.getBookmarkByUrl(url)
    }

    fun getBookmarkWithTags(id: String): Flow<BookmarkWithTags?> {
        return repository.getBookmarkWithTags(id)
    }

    fun markAsOpened(bookmarkId: String) {
        viewModelScope.launch {
            repository.updateLastOpenedAt(bookmarkId)
        }
    }

    fun deleteBookmark(id: String) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun deleteAllBookmarks() {
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    fun deleteAllArchivedBookmarks() {
        viewModelScope.launch {
            repository.deleteAllArchived()
        }
    }

    fun updateBookmarkWithTags(
        bookmark: BookmarkEntity,
        tags: List<String>
    ) {
        viewModelScope.launch {
            repository.updateBookmarkWithTags(bookmark, tags)
        }
    }

    suspend fun exportBookmarksJson(): String {
        return repository.exportBookmarksJson()
    }

    suspend fun exportBookmarksCsv(): String {
        return repository.exportBookmarksCsv()
    }

    suspend fun importBookmarksJson(content: String): ImportResult {
        return repository.importBookmarksJson(content)
    }

    suspend fun importBookmarksCsv(content: String): ImportResult {
        return repository.importBookmarksCsv(content)
    }

    suspend fun exportBookmarksHtml(): String {
        return repository.exportBookmarksHtml()
    }

    suspend fun importBookmarksHtml(content: String): ImportResult {
        return repository.importBookmarksHtml(content)
    }
}