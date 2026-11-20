package com.emilioaugust.seenread

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.emilioaugust.seenread.data.database.AppDatabase
import com.emilioaugust.seenread.data.repository.BookmarkRepository
import com.emilioaugust.seenread.utils.LocaleHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class ShareReceiverActivity : ComponentActivity() {
    private lateinit var repository: BookmarkRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val app = applicationContext
        val db = AppDatabase.getInstance(app)
        repository = BookmarkRepository(
            dao = db.bookmarkDao(),
            tagDao = db.tagDao(),
            database = db
        )

        setContent {
            ShareReceiverScreen()
        }

        handleShare(intent)
    }

    private fun handleShare(intent: Intent?) {
        if (intent?.action != Intent.ACTION_SEND) {
            finish()
            return
        }

        if (intent.type?.startsWith("text/") != true) {
            finish()
            return
        }

        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            ?: intent.clipData?.getItemAt(0)?.text?.toString()

        val url = sharedText?.let(::extractUrl)

        if (url == null) {
            Toast.makeText(applicationContext, "Couldn't find URL", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val result = runCatching { repository.saveCopiedUrl(url) }
                .onFailure { Log.e("ShareReceiver", "Error while saving", it) }
                .getOrDefault(false)

            withContext(Dispatchers.Main) {
                val msg = if (result) {
                    R.string.bookmark_successfully_saved
                } else {
                    R.string.bookmark_already_exists
                }
                Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun extractUrl(text: String): String? {
        val matcher = Patterns.WEB_URL.matcher(text)
        if (!matcher.find()) return null
        return matcher.group()?.trim()?.trimEnd('.', ',', '!', '?', ')', ']')
    }
}

@Composable
fun ShareReceiverScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 24.dp,
                    vertical = 18.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50)
                )

                Spacer(Modifier.width(12.dp))

                Text(
                    text = stringResource(R.string.saved_to_seenread),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}