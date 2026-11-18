package com.emilioaugust.seenread.ui.enums

import androidx.annotation.StringRes
import com.emilioaugust.seenread.R

enum class AppLanguage(val code: String, @StringRes val titleRes: Int) {
    ENGLISH("en", R.string.english),
    RUSSIAN("ru", R.string.russian),
}