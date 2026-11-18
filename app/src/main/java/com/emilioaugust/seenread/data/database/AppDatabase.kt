package com.emilioaugust.seenread.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.emilioaugust.seenread.data.dao.BookmarkDao
import com.emilioaugust.seenread.data.dao.TagDao
import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.data.entity.TagEntity
import com.emilioaugust.seenread.data.entity.BookmarkTagCrossRef

@Database(entities = [BookmarkEntity::class, TagEntity::class, BookmarkTagCrossRef::class], version = 5)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun tagDao(): TagDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance =
                    Room.databaseBuilder(
                        context.applicationContext, AppDatabase::class.java,
                        "bookmarks_database"
                    ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}