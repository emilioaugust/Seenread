package com.emilioaugust.seenread.ui.enums

sealed class Screen(val route: String) {
    object Recent : Screen("recent")
    object Library : Screen("library")
    object Settings : Screen("settings")
    object Welcome : Screen("welcome")
    object Add : Screen("add")
    object About : Screen("about")
    object Archive : Screen("archive")
    object Favorite : Screen("favorite")
    object Review : Screen("review")
}