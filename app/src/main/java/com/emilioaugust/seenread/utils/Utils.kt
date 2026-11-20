package com.emilioaugust.seenread.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.net.toUri
import com.emilioaugust.seenread.R
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

fun formatSavedTime(context: Context, timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    val timeAgo = when {
        diff < hour -> context.getString(R.string.m_ago, (diff / minute).coerceAtLeast(1))
        diff < day -> context.getString(R.string.h_ago, diff / hour)
        diff < week -> context.getString(R.string.d_ago, diff / day)
        diff < month -> context.getString(R.string.w_ago, diff / week)
        diff < year -> context.getString(R.string.mo_ago, diff / month)
        else -> context.getString(R.string.y_ago, diff / year)
    }

    return context.getString(R.string.saved_ago, timeAgo)
}

fun formatLastOpened(context: Context, timestamp: Long?): String {
    if (timestamp == null) return context.getString(R.string.never_opened)

    val diff = (System.currentTimeMillis() - timestamp)
        .coerceAtLeast(0L)

    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    val week = 7 * day
    val month = 30 * day
    val year = 365 * day

    val result = when {
        diff < minute -> context.getString(R.string.just_now)
        diff < day -> context.getString(R.string.h_ago, diff / hour)
        diff < week -> context.getString(R.string.d_ago, diff / day)
        diff < month -> context.getString(R.string.w_ago, diff / week)
        diff < year -> context.getString(R.string.mo_ago, diff / month)
        else -> context.getString(R.string.y_ago, diff / year)
    }

    return context.getString(R.string.opened, result)
}

fun formatArchivedTime(context: Context, timestamp: Long?): String {
    if (timestamp == null) return context.getString(R.string.unknown_archive_date)

    val now = System.currentTimeMillis()
    val diff = (now - timestamp).coerceAtLeast(0L)

    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000

    return when {
        minutes < 1 -> context.getString(R.string.archived_just_now)
        minutes < 60 -> context.getString(R.string.archived_min_ago, minutes)
        hours < 24 -> context.getString(R.string.archived_h_ago, hours)
        days < 30 -> context.getString(R.string.archived_d_ago, days)
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
