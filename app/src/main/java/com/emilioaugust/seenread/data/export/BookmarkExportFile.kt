package com.emilioaugust.seenread.data.export

import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import kotlinx.serialization.Serializable

@Serializable
data class BookmarkExportFile(
    val version: Int = 1,
    val bookmarks: List<BookmarkExportDto>
)

@Serializable
data class BookmarkExportDto(
    val id: String,
    val url: String,
    val title: String?,
    val coverImageUrl: String?,
    val faviconUrl: String?,
    val intention: String,
    val note: String?,
    val isFavorite: Boolean,
    val isArchived: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long?,
    val lastOpenedAt: Long?,
    val tags: List<String>
)

fun BookmarkWithTags.toExportDto() = BookmarkExportDto(
    id = bookmark.id,
    url = bookmark.url,
    title = bookmark.title,
    coverImageUrl = bookmark.coverImageUrl,
    faviconUrl = bookmark.faviconUrl,
    intention = bookmark.intention.name,
    note = bookmark.note,
    isFavorite = bookmark.isFavorite,
    isArchived = bookmark.isArchived,
    createdAt = bookmark.createdAt,
    updatedAt = bookmark.updatedAt,
    archivedAt = bookmark.archivedAt,
    lastOpenedAt = bookmark.lastOpenedAt,
    tags = tags.map { it.name }
)