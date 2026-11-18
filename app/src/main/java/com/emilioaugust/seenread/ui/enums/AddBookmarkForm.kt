package com.emilioaugust.seenread.ui.enums

import com.emilioaugust.seenread.data.entity.BookmarkEntity
import com.emilioaugust.seenread.utils.getFaviconUrl
import java.util.UUID

data class AddBookmarkForm(
    val url: String = "",
    val title: String? = "",
    val intention: Intention = Intention.READ,
    val note: String? = "",
    val tags: List<String> = emptyList()
)

fun AddBookmarkForm.toBookmarkEntity(): BookmarkEntity {
    val now = System.currentTimeMillis()

    return BookmarkEntity(
        id = UUID.randomUUID().toString(),
        url = url.trim(),
        title = title,
        note = note,
        faviconUrl = getFaviconUrl(url.trim()),
        intention = intention,
        createdAt = now,
        updatedAt = now
    )
}