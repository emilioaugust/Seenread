package com.emilioaugust.seenread.ui.enums

import androidx.annotation.StringRes
import com.emilioaugust.seenread.R

enum class Intention(@StringRes var title: Int, var icon: Int, var selectedIcon: Int) {
    READ(R.string.read, R.drawable.read, R.drawable.selected_read),
    LEARN(R.string.learn, R.drawable.learn, R.drawable.selected_learn),
    REFERENCE(R.string.reference, R.drawable.reference, R.drawable.selecteed_reference),
    TRY(R.string.try_tag, R.drawable.resource_try, R.drawable.selected_try),
    BUY(R.string.buy, R.drawable.buy, R.drawable.selected_buy)
}