package com.emilioaugust.seenread.data.export


import com.emilioaugust.seenread.data.entity.BookmarkWithTags
import kotlinx.serialization.json.Json

object BookmarkCsv {

    private val headers = listOf(
        "id", "url", "title", "coverImageUrl", "faviconUrl",
        "intention", "note", "isFavorite", "isArchived",
        "createdAt", "updatedAt", "archivedAt", "lastOpenedAt",
        "tags"
    )

    fun export(bookmarks: List<BookmarkWithTags>): String {
        val rows = bookmarks.map { item ->
            val b = item.bookmark

            listOf(
                b.id,
                b.url,
                b.title.orEmpty(),
                b.coverImageUrl.orEmpty(),
                b.faviconUrl.orEmpty(),
                b.intention.name,
                b.note.orEmpty(),
                b.isFavorite.toString(),
                b.isArchived.toString(),
                b.createdAt.toString(),
                b.updatedAt.toString(),
                b.archivedAt?.toString().orEmpty(),
                b.lastOpenedAt?.toString().orEmpty(),
                Json.encodeToString(item.tags.map { it.name })
            )
        }

        return (listOf(headers) + rows)
            .joinToString("\n") { row ->
                row.joinToString(",") { escape(it) }
            }
    }

    fun import(content: String): List<BookmarkExportDto> {
        val rows = parseCsv(content)
        require(rows.isNotEmpty()) { "CSV is empty" }

        val header = rows.first()
        require(header == headers) {
            "Wrong CSV format"
        }

        return rows.drop(1)
            .filter { it.any(String::isNotBlank) }
            .map { row ->
                require(row.size == headers.size) {
                    "Incorrect CSV"
                }

                fun value(name: String) =
                    row[headers.indexOf(name)]

                BookmarkExportDto(
                    id = value("id"),
                    url = value("url"),
                    title = value("title").ifBlank { null },
                    coverImageUrl = value("coverImageUrl")
                        .ifBlank { null },
                    faviconUrl = value("faviconUrl")
                        .ifBlank { null },
                    intention = value("intention"),
                    note = value("note").ifBlank { null },
                    isFavorite = value("isFavorite").toBooleanStrict(),
                    isArchived = value("isArchived").toBooleanStrict(),
                    createdAt = value("createdAt").toLong(),
                    updatedAt = value("updatedAt").toLong(),
                    archivedAt = value("archivedAt")
                        .ifBlank { null }?.toLong(),
                    lastOpenedAt = value("lastOpenedAt")
                        .ifBlank { null }?.toLong(),
                    tags = Json.decodeFromString(value("tags"))
                )
            }
    }

    private fun escape(value: String): String {
        return "\"" + value.replace("\"", "\"\"") + "\""
    }

    private fun parseCsv(content: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var row = mutableListOf<String>()
        val field = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < content.length) {
            val char = content[i]

            when {
                char == '"' && insideQuotes &&
                        i + 1 < content.length &&
                        content[i + 1] == '"' -> {
                    field.append('"')
                    i++
                }

                char == '"' -> {
                    insideQuotes = !insideQuotes
                }

                char == ',' && !insideQuotes -> {
                    row.add(field.toString())
                    field.clear()
                }

                (char == '\n' || char == '\r') && !insideQuotes -> {
                    row.add(field.toString())
                    field.clear()

                    if (row.any { it.isNotEmpty() }) {
                        rows.add(row)
                    }

                    row = mutableListOf()

                    if (char == '\r' &&
                        i + 1 < content.length &&
                        content[i + 1] == '\n'
                    ) {
                        i++
                    }
                }

                else -> field.append(char)
            }

            i++
        }

        require(!insideQuotes) { "Not closed quote in CSV" }

        if (field.isNotEmpty() || row.isNotEmpty()) {
            row.add(field.toString())
            rows.add(row)
        }

        return rows
    }
}
