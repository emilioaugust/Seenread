package com.emilioaugust.seenread.data.repository

import androidx.room.withTransaction
import com.emilioaugust.seenread.data.dao.BookmarkDao
import com.emilioaugust.seenread.data.dao.TagDao
import com.emilioaugust.seenread.data.database.AppDatabase
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.entity.BookmarkTagCrossRef
import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import com.emilioaugust.seenread.data.entity.TagEntity
import com.emilioaugust.seenread.data.export.BookmarkCsv
import com.emilioaugust.seenread.data.export.BookmarkExportDto
import com.emilioaugust.seenread.data.export.BookmarkExportFile
import com.emilioaugust.seenread.data.export.BookmarkHtml
import com.emilioaugust.seenread.data.export.toExportDto
import com.emilioaugust.seenread.ui.enums.Intention
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import java.util.UUID

data class ImportResult(
    val imported: Int,
    val skipped: Int
)

class BookmarkRepository(
    private val dao: BookmarkDao,
    private val tagDao: TagDao,
    private val database: AppDatabase
) {

    fun getAllItems(): Flow<List<BookmarkWithTags>> {
        return dao.observeActiveWithTags()
    }

    fun getAllItemsForLibrary(): Flow<List<BookmarkWithTags>> {
        return dao.observeActiveWithTagsForLibrary()
    }

    fun getAllArchived(): Flow<List<BookmarkEntity>> {
        return dao.observeArchived()
    }

    fun getAllFavorites(): Flow<List<BookmarkEntity>> {
        return dao.observeFavorites()
    }

    fun getAllWithIntentions(
        intention: Intention?
    ): Flow<List<BookmarkWithTags>> {
        return dao.observeWithIntentions(intention)
    }

    fun getBookmarksOlderThan14Days(): Flow<List<BookmarkEntity>> {
        return dao.getBookmarksOlderThan14Days()
    }

    fun getBookmarkById(id: String) = dao.getById(id)

    fun getBookmarkWithTags(id: String) =
        tagDao.getBookmarkWithTags(id)

    fun getBookmarkByUrl(url: String) = dao.getByUrl(url)

    suspend fun updateLastOpenedAt(bookmarkId: String) {
        dao.updateLastOpenedAt(
            bookmarkId = bookmarkId,
            timestamp = System.currentTimeMillis()
        )
    }

    suspend fun insertBookmarkWithTags(
        bookmark: BookmarkEntity,
        tags: List<String>
    ) {
        dao.insert(bookmark)

        tags.forEach { tag ->
            tagDao.addTagToBookmark(
                bookmarkId = bookmark.id,
                tagName = tag
            )
        }
    }

    suspend fun saveCopiedUrl(url: String): Boolean {
        val now = System.currentTimeMillis()

        val bookmark = BookmarkEntity(
            id = UUID.randomUUID().toString(),
            url = url,
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

        return database.withTransaction {
            val insertedId = dao.insertBookmarkIgnore(bookmark)
            insertedId != -1L
        }
    }

    suspend fun updateBookmarkWithTags(
        bookmark: BookmarkEntity,
        tags: List<String>
    ) {
        dao.update(bookmark)

        tagDao.deleteTagsForBookmark(bookmark.id)

        tags
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase() }
            .forEach { tagName ->
                tagDao.addTagToBookmark(
                    bookmarkId = bookmark.id,
                    tagName = tagName
                )
            }
    }

    suspend fun setArchived(
        id: String,
        isArchived: Boolean,
        updatedAt: Long
    ) {
        dao.setArchived(id, isArchived, updatedAt)
    }

    suspend fun setFavorite(
        id: String,
        isFavorite: Boolean,
        updatedAt: Long
    ) {
        dao.setFavorite(id, isFavorite, updatedAt)
    }

    suspend fun deleteById(id: String) {
        dao.deleteById(id)
    }

    suspend fun deleteAll() {
        dao.deleteAll()
    }

    suspend fun deleteAllArchived() {
        dao.deleteAllArchived()
    }

    suspend fun updateBookmark(bookmark: BookmarkEntity) {
        dao.update(bookmark)
    }

    // JSON

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportBookmarksJson(): String {
        val bookmarks = dao.getAllBookmarksWithTags()

        val exportFile = BookmarkExportFile(
            bookmarks = bookmarks.map {
                it.toExportDto()
            }
        )

        return json.encodeToString(exportFile)
    }

    suspend fun importBookmarksJson(
        content: String
    ): ImportResult {
        val exportFile = json.decodeFromString<BookmarkExportFile>(
            content
        )

        require(exportFile.version == 1) {
            "Неподдерживаемая версия файла"
        }

        return importExportDtos(exportFile.bookmarks)
    }

    // CSV

    suspend fun exportBookmarksCsv(): String {
        return BookmarkCsv.export(
            dao.getAllBookmarksWithTags()
        )
    }

    suspend fun importBookmarksCsv(
        content: String
    ): ImportResult {
        val bookmarks = BookmarkCsv.import(content)

        return importExportDtos(bookmarks)
    }

    // HTML

    suspend fun exportBookmarksHtml(): String {
        val bookmarks = dao.getAllBookmarksWithTags()
        return BookmarkHtml.export(bookmarks)
    }

    suspend fun importBookmarksHtml(content: String): ImportResult {
        val bookmarks = BookmarkHtml.import(content)
        return importExportDtos(bookmarks)
    }

    // Общий импорт для JSON и CSV

    private suspend fun importExportDtos(
        bookmarks: List<BookmarkExportDto>
    ): ImportResult {

        var imported = 0
        var skipped = 0

        database.withTransaction {

            bookmarks.forEach { dto ->

                require(dto.id.isNotBlank()) {
                    "Обнаружена закладка без ID"
                }

                require(dto.url.isNotBlank()) {
                    "Обнаружена закладка без URL"
                }

                val intention = runCatching {
                    Intention.valueOf(dto.intention)
                }.getOrDefault(Intention.READ)

                val bookmark = BookmarkEntity(
                    id = dto.id,
                    url = dto.url,
                    title = dto.title,
                    coverImageUrl = dto.coverImageUrl,
                    faviconUrl = dto.faviconUrl,
                    intention = intention,
                    note = dto.note,
                    isFavorite = dto.isFavorite,
                    isArchived = dto.isArchived,
                    createdAt = dto.createdAt,
                    updatedAt = dto.updatedAt,
                    archivedAt = dto.archivedAt,
                    lastOpenedAt = dto.lastOpenedAt
                )

                val insertedId = dao.insertBookmarkIgnore(bookmark)

                if (insertedId == -1L) {
                    skipped++
                    return@forEach
                }

                dto.tags
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                    .distinctBy { it.lowercase() }
                    .forEach tagLoop@{ tagName ->

                        tagDao.insertTag(
                            TagEntity(name = tagName)
                        )

                        val tag = tagDao.getTagByName(tagName)
                            ?: return@tagLoop

                        tagDao.insertCrossRef(
                            BookmarkTagCrossRef(
                                bookmarkId = bookmark.id,
                                tagId = tag.id
                            )
                        )
                    }

                imported++
            }
        }

        return ImportResult(
            imported = imported,
            skipped = skipped
        )
    }
}
