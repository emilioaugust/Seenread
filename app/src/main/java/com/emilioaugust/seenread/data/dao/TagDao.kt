package com.emilioaugust.seenread.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.emilioaugust.seenread.data.entity.BookmarkTagCrossRef
import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import com.emilioaugust.seenread.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTag(tag: TagEntity): Long

    @Query("SELECT * FROM tags WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getTagByName(name: String): TagEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(ref: BookmarkTagCrossRef)

    @Transaction
    @Query("SELECT * FROM bookmarks WHERE id = :bookmarkId")
    fun getBookmarkWithTags(bookmarkId: String): Flow<BookmarkWithTags?>

    @Transaction
    @Query("SELECT * FROM bookmarks")
    fun getAllBookmarksWithTags(): Flow<List<BookmarkWithTags>>

    @Transaction
    suspend fun addTagToBookmark(
        bookmarkId: String,
        tagName: String
    ) {
        val normalizedName = tagName.trim()

        if (normalizedName.isBlank()) return

        insertTag(TagEntity(name = normalizedName))

        val tag = getTagByName(normalizedName) ?: return

        insertCrossRef(
            BookmarkTagCrossRef(
                bookmarkId = bookmarkId,
                tagId = tag.id
            )
        )
    }

    @Query("DELETE FROM bookmark_tags WHERE bookmarkId = :bookmarkId")
    suspend fun deleteTagsForBookmark(bookmarkId: String)
}