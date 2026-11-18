package com.emilioaugust.seenread.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import com.emilioaugust.seenread.ui.enums.Intention
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BookmarkEntity)

    @Transaction
    @Query("SELECT * FROM bookmarks")
    suspend fun getAllBookmarksWithTags(): List<BookmarkWithTags>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBookmarkIgnore(
        bookmark: BookmarkEntity
    ): Long

    @Update
    suspend fun update(bookmark: BookmarkEntity)

    @Query("UPDATE bookmarks SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
    suspend fun setFavorite(id: String, isFavorite: Boolean, updatedAt: Long)

    @Query("""
    UPDATE bookmarks
    SET isArchived = :isArchived,
        updatedAt = :updatedAt,
        archivedAt = CASE
            WHEN :isArchived = 1 THEN :updatedAt
            ELSE NULL
        END
    WHERE id = :id
""")
    suspend fun setArchived(
        id: String,
        isArchived: Boolean,
        updatedAt: Long
    )

    @Query("""
    UPDATE bookmarks
    SET lastOpenedAt = :timestamp
    WHERE id = :bookmarkId
""")
    suspend fun updateLastOpenedAt(
        bookmarkId: String,
        timestamp: Long
    )

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM bookmarks")
    suspend fun deleteAll()

    @Query("DELETE FROM bookmarks WHERE isArchived = 1")
    suspend fun deleteAllArchived()

    @Query("SELECT * FROM bookmarks WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<BookmarkEntity?>

    @Query("SELECT * FROM bookmarks WHERE url = :url LIMIT 1")
    fun getByUrl(url: String): Flow<BookmarkEntity>

    @Transaction
    @Query("""
    SELECT * FROM bookmarks
    WHERE isArchived = 0
    ORDER BY createdAt DESC
    LIMIT 15
    """)
    fun observeActiveWithTags(): Flow<List<BookmarkWithTags>>

    @Transaction
    @Query("""
    SELECT * FROM bookmarks
    WHERE isArchived = 0
    ORDER BY createdAt DESC
    """)
    fun observeActiveWithTagsForLibrary(): Flow<List<BookmarkWithTags>>

    @Query("""
        SELECT * FROM bookmarks
        WHERE isArchived = 1
        ORDER BY createdAt DESC
    """)
    fun observeArchived(): Flow<List<BookmarkEntity>>

    @Query("""
        SELECT * FROM bookmarks
        WHERE isFavorite = 1 AND isArchived = 0
        ORDER BY createdAt DESC
    """)
    fun observeFavorites(): Flow<List<BookmarkEntity>>

    @Transaction
    @Query("""
    SELECT * FROM bookmarks
    WHERE isArchived = 0
    AND (:intention IS NULL OR intention = :intention)
    ORDER BY createdAt DESC
""")
    fun observeWithIntentions(intention: Intention?): Flow<List<BookmarkWithTags>>

    @Query("""
    SELECT * FROM bookmarks
    WHERE createdAt <= :threshold AND isArchived = 0
    ORDER BY createdAt ASC
""")
    fun getBookmarksOlderThan14Days(
        threshold: Long = System.currentTimeMillis() - 14L * 24 * 60 * 60 * 1000
    ): Flow<List<BookmarkEntity>>
}