package com.emilioaugust.seenread.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun getFaviconUrl(rawUrl: String): String? {
    val normalizedUrl = if (
        rawUrl.startsWith("https://", ignoreCase = true) ||
        rawUrl.startsWith("http://", ignoreCase = true)
    ) {
        rawUrl
    } else {
        "https://$rawUrl"
    }

    val host = normalizedUrl.toUri()
        .host
        ?.lowercase()
        ?: return null

    return "https://www.google.com/s2/favicons?domain=$host&sz=64"
}

fun normalizeUrl(rawUrl: String): String? {
    val value = rawUrl.trim()

    if (value.isBlank()) return null

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

fun getClearDomain(rawUrl: String): String? {
    val input = rawUrl.trim()
    if (input.isEmpty()) return null

    val normalizedUrl = if (
        input.startsWith("https://", ignoreCase = true) ||
        input.startsWith("http://", ignoreCase = true)
    ) {
        input
    } else {
        "https://$input"
    }

    val host = normalizedUrl.toUri()
        .host
        ?.lowercase()
        ?: return null

    return host.removePrefix("www.")
}

fun formatSavedTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    val timeAgo = when {
        diff < hour -> "${(diff / minute).coerceAtLeast(1)}m ago"
        diff < day -> "${diff / hour}h ago"
        diff < week -> "${diff / day}d ago"
        diff < month -> "${diff / week}w ago"
        diff < year -> "${diff / month}mo ago"
        else -> "${diff / year}y ago"
    }

    return "Saved $timeAgo"
}

fun formatLastOpened(timestamp: Long?): String {
    if (timestamp == null) return "Never opened"

    val diff = (System.currentTimeMillis() - timestamp)
        .coerceAtLeast(0L)

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    val result = when {
        diff < minute -> "Just now"
        diff < hour -> "${diff / minute}m ago"
        diff < day -> "${diff / hour}h ago"
        diff < week -> "${diff / day}d ago"
        diff < month -> "${diff / week}w ago"
        diff < year -> "${diff / month}mo ago"
        else -> "${diff / year}y ago"
    }

    return "Opened $result"
}

fun formatArchivedTime(timestamp: Long?): String {
    if (timestamp == null) return "Unknown archive date"

    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)

    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000

    return when {
        minutes < 1 -> "Archived just now"
        minutes < 60 -> "Archived ${minutes}min ago"
        hours < 24 -> "Archived ${hours}h ago"
        days < 30 -> "Archived ${days}d ago"
        else -> {
            val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            "Archived ${formatter.format(Date(timestamp))}"
        }
    }
}

fun openExternalBrowser(context: Context, url: String) {
    val normalizedUrl = if (
        url.startsWith("https://", true) ||
        url.startsWith("http://", true)
    ) {
        url
    } else {
        "https://$url"
    }

    val uri = normalizedUrl.toUri()

    if (uri.scheme != "https" && uri.scheme != "http") {
        return
    }

    val intent = Intent(Intent.ACTION_VIEW, uri)

    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            "Couldn't find browser",
            Toast.LENGTH_SHORT
        ).show()
    }
}
