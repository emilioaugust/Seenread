package com.emilioaugust.seenread.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.emilioaugust.seenread.ui.enums.Intention

@Entity(tableName = "bookmarks", indices = [Index(value = ["url"], unique = true)])
data class BookmarkEntity(
    @PrimaryKey val id: String,
    val url: String,
    val title: String?,
    val coverImageUrl: String? = null,
    val faviconUrl: String? = null,
    val intention: Intention = Intention.READ,
    val note: String? = null,
    val isFavorite: Boolean = false,
    val isArchived: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
    val archivedAt: Long? = null,
    val lastOpenedAt: Long? = null
)