package com.emilioaugust.seenread.data.export

import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import com.emilioaugust.seenread.ui.enums.Intention
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.util.UUID

object BookmarkHtml {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    fun export(bookmarks: List<BookmarkWithTags>): String {
        return buildString {
            appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
            appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
            appendLine("<TITLE>Recollect Bookmarks</TITLE>")
            appendLine("<H1>Recollect Bookmarks</H1>")
            appendLine("<DL><p>")

            bookmarks.forEach { item ->
                val bookmark = item.bookmark
                val dto = item.toExportDto()

                val title = escape(bookmark.title ?: bookmark.url)
                val url = escape(bookmark.url)
                val createdAt = bookmark.createdAt / 1000

                val tagsJson = escape(json.encodeToString(dto.tags))
                val note = escape(bookmark.note.orEmpty())
                val intention = escape(bookmark.intention.name)

                appendLine(
                    """    <DT><A HREF="$url" ADD_DATE="$createdAt" """ +
                            """DATA-RECOLLECT-ID="${escape(bookmark.id)}" """ +
                            """DATA-RECOLLECT-TAGS="$tagsJson" """ +
                            """DATA-RECOLLECT-NOTE="$note" """ +
                            """DATA-RECOLLECT-INTENTION="$intention" """ +
                            """DATA-RECOLLECT-FAVORITE="${bookmark.isFavorite}" """ +
                            """DATA-RECOLLECT-ARCHIVED="${bookmark.isArchived}" """ +
                            """DATA-RECOLLECT-CREATED="${bookmark.createdAt}" """ +
                            """DATA-RECOLLECT-UPDATED="${bookmark.updatedAt}" """ +
                            """DATA-RECOLLECT-ARCHIVED-AT="${bookmark.archivedAt ?: ""}" """ +
                            """DATA-RECOLLECT-LAST-OPENED="${bookmark.lastOpenedAt ?: ""}" """ +
                            """DATA-RECOLLECT-COVER="${escape(bookmark.coverImageUrl.orEmpty())}" """ +
                            """DATA-RECOLLECT-FAVICON="${escape(bookmark.faviconUrl.orEmpty())}">""" +
                            "$title</A>"
                )
            }

            appendLine("</DL><p>")
        }
    }

    fun import(content: String): List<BookmarkExportDto> {
        val document = Jsoup.parse(content)
        val links = document.select("a[href]")

        return links.mapNotNull { link ->
            val url = link.attr("href").trim()

            if (url.isBlank()) return@mapNotNull null

            val now = System.currentTimeMillis()

            val createdAt = link.attr("data-recollect-created")
                .toLongOrNull()
                ?: link.attr("add_date")
                    .toLongOrNull()
                    ?.times(1000)
                ?: now

            val tagsFromAttribute = runCatching {
                json.decodeFromString<List<String>>(
                    link.attr("data-recollect-tags")
                )
            }.getOrDefault(emptyList())

            // Папки браузера тоже превращаем в теги
            val folderTags = link.parents()
                .filter { it.tagName() == "dl" }
                .mapNotNull { dl ->
                    dl.previousElementSibling()
                        ?.takeIf { it.tagName() == "dt" }
                        ?.selectFirst("h3")
                        ?.text()
                        ?.trim()
                }
                .reversed()

            val tags = (folderTags + tagsFromAttribute)
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }

            val intention = runCatching {
                Intention.valueOf(
                    link.attr("data-recollect-intention")
                )
            }.getOrDefault(Intention.READ)

            BookmarkExportDto(
                id = link.attr("data-recollect-id")
                    .takeIf { it.isNotBlank() }
                    ?: UUID.randomUUID().toString(),

                url = url,
                title = link.text().trim().ifBlank { null },

                coverImageUrl = link.attr("data-recollect-cover")
                    .takeIf { it.isNotBlank() },

                faviconUrl = link.attr("data-recollect-favicon")
                    .takeIf { it.isNotBlank() },

                intention = intention.name,

                note = link.attr("data-recollect-note")
                    .takeIf { it.isNotBlank() },

                isFavorite = link.attr("data-recollect-favorite")
                    .toBooleanStrictOrNull() ?: false,

                isArchived = link.attr("data-recollect-archived")
                    .toBooleanStrictOrNull() ?: false,

                createdAt = createdAt,

                updatedAt = link.attr("data-recollect-updated")
                    .toLongOrNull() ?: createdAt,

                archivedAt = link.attr("data-recollect-archived-at")
                    .toLongOrNull(),

                lastOpenedAt = link.attr("data-recollect-last-opened")
                    .toLongOrNull(),

                tags = tags
            )
        }
    }

    private fun escape(value: String): String {
        return value
            .replace("&", "&amp;")
            .replace("\"", "&quot;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("'", "&#39;")
    }
}
