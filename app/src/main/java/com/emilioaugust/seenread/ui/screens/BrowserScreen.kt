package com.emilioaugust.seenread.ui.screens

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    url: String,
    onBack: () -> Unit,
    viewModel: BookmarkViewModel
) {
    val bookmark by viewModel.getBookmarkByUrl(url).collectAsState(initial = null)

    if (bookmark == null) {
        Box(modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
            return
        }
    }

    val currentBookmark = bookmark ?: return

    var webView by remember {
        mutableStateOf<WebView?>(null)
    }

    var pageTitle by remember {
        mutableStateOf("Loading...")
    }

    var progress by remember {
        mutableIntStateOf(0)
    }

    BackHandler {
        if (webView?.canGoBack() == true) {
            webView?.goBack()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pageTitle, maxLines = 1, overflow = TextOverflow.Ellipsis,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentDescription = null
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { webView?.reload() }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            contentDescription = null
                        )
                    }

                    BrowserTopActions(
                        onFavorite = {
                            viewModel.setFavorite(
                                currentBookmark.id,
                                !currentBookmark.isFavorite,
                                System.currentTimeMillis()
                            )
                        },
                        onShare = {

                        },
                        isFavorite = currentBookmark.isFavorite
                    )
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
            if (progress < 100) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                progress = 0
                            }

                            override fun onPageFinished(
                                view: WebView?,
                                url: String?
                            ) {
                                pageTitle = view?.title ?: context.getString(R.string.unknown)
                            }
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(
                                view: WebView?,
                                newProgress: Int
                            ) {
                                progress = newProgress
                            }
                        }

                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true

                        loadUrl(url)
                        webView = this
                    }
                },
                update = { view ->
                    webView = view
                },
                onRelease = { view ->
                    view.stopLoading()
                    view.destroy()
                    webView = null
                }
            )
        }
    }
}

@Composable
fun BrowserTopActions(onFavorite: () -> Unit, onShare: () -> Unit, isFavorite: Boolean) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(Icons.Default.MoreVert, contentDescription = "More",
            tint = MaterialTheme.colorScheme.onPrimaryContainer)
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        DropdownMenuItem(
            text = { Text(
                if (!isFavorite) {
                    stringResource(R.string.add_to_favorites)
                } else {
                    stringResource(R.string.remove_from_favorites)
                },  color = MaterialTheme.colorScheme.onPrimaryContainer
            )
                   },
            leadingIcon = {
                Icon(
                    if(!isFavorite) {
                        Icons.Default.FavoriteBorder
                    } else {
                        Icons.Default.Favorite
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) },
            onClick = {
                expanded = false
                onFavorite()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.share),  color = MaterialTheme.colorScheme.onPrimaryContainer) },
            leadingIcon = {
                Icon(
                    Icons.Default.Share,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) },
            onClick = {
                expanded = false
                onShare()
            }
        )
    }
}
