package com.emilioaugust.seenread.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.util.concurrent.TimeUnit

object PageMetadataFetcher {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    suspend fun fetchMetadata(url: String): PageMetadata? =
        withContext(Dispatchers.IO) {
            try {
                val normalizedUrl = normalizeUrl(url)
                    ?: return@withContext null

                val request = Request.Builder()
                    .url(normalizedUrl)
                    .header(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 10) " +
                                "AppleWebKit/537.36 Chrome/131.0.0.0 Mobile Safari/537.36"
                    )
                    .build()

                client.newCall(request).execute().use { response ->

                    if (!response.isSuccessful) {
                        return@withContext null
                    }

                    val html = response.body?.string()
                        ?: return@withContext null

                    val finalUrl = response.request.url.toString()

                    val document = Jsoup.parse(html, finalUrl)

                    val title = document.title()
                        .trim()
                        .takeIf { it.isNotBlank() }

                    val faviconFromHtml = document
                        .select("link[rel][href]")
                        .firstNotNullOfOrNull { element ->

                            val rel = element
                                .attr("rel")
                                .lowercase()
                                .split(Regex("\\s+"))

                            val isIcon = rel.any {
                                it == "icon" ||
                                        it == "shortcut" ||
                                        it == "apple-touch-icon" ||
                                        it == "apple-touch-icon-precomposed"
                            }

                            if (isIcon) {
                                element.absUrl("href")
                                    .takeIf { it.isNotBlank() }
                                    ?.takeIf {
                                        it.startsWith("https://") ||
                                                it.startsWith("http://")
                                    }
                            } else {
                                null
                            }
                        }

                    val faviconUrl = faviconFromHtml
                        ?: getFaviconUrl(finalUrl)

                    PageMetadata(
                        title = title,
                        faviconUrl = faviconUrl
                    )
                }

            } catch (e: Exception) {
                null
            }
        }

    private fun normalizeUrl(rawUrl: String): String? {
        val value = rawUrl.trim()

        val normalized = if (
            value.startsWith("https://", true) ||
            value.startsWith("http://", true)
        ) {
            value
        } else {
            "https://$value"
        }

        return normalized.takeIf {
            it.toHttpUrlOrNull() != null
        }
    }

    private fun getFaviconUrl(rawUrl: String): String? {
        val host = rawUrl.toHttpUrlOrNull()?.host
            ?: return null

        return "https://www.google.com/s2/favicons?domain=$host&sz=64"
    }
}



data class PageMetadata(
    val title: String?,
    val faviconUrl: String?
)