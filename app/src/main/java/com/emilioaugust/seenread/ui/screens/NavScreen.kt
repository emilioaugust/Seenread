package com.emilioaugust.seenread.ui.screens

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.emilioaugust.seenread.data.viewmodel.BookmarkViewModel
import com.emilioaugust.seenread.data.viewmodel.SettingsViewModel
import com.emilioaugust.seenread.ui.enums.Screen

@Composable
fun NavScreen(viewModel: BookmarkViewModel, settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavHost(
        navController = navController, startDestination = Screen.Recent.route

    ) {
        composable(Screen.Welcome.route) {
            WelcomeScreen(
                onNavigateTo = { navController.navigate(Screen.Recent.route) },
                onNavigateToAbout = { navController.navigate(Screen.About.route) }
            )
        }
        composable(Screen.Recent.route) {
            RecentScreen(
                navController, viewModel, settingsViewModel,
                onNavigateToDetails = { bookmarkId ->
                    navController.navigate("bookmark_detail/$bookmarkId")
                }
            )
        }
        composable(Screen.Add.route) { AddScreen(
            viewModel, onBack = { navController.popBackStack() })}

        composable("bookmark_detail/{bookmarkId}") {backStackEntry ->
            val bookmarktId = backStackEntry.arguments?.getString("bookmarkId").toString()
            DetailScreen(bookmarktId, viewModel, settingsViewModel, navController,
                onBack = { navController.popBackStack() })
        }

        composable(Screen.About.route) { AboutScreen(onBack = { navController.popBackStack() }) }
        composable(Screen.Archive.route) {
            ArchiveScreen(viewModel, onBack = { navController.popBackStack() },
                onNavigateToDetails = { bookmarkId ->
                    navController.navigate("bookmark_detail/$bookmarkId")
                }
            )
        }

        composable(Screen.Favorite.route) {
            FavoritesScreen(viewModel, onBack = { navController.popBackStack() },
                onNavigateToDetails = { bookmarkId ->
                    navController.navigate("bookmark_detail/$bookmarkId")
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen(
                navController, viewModel, settingsViewModel,
                onNavigateToDetails = { bookmarkId ->
                    navController.navigate("bookmark_detail/$bookmarkId")
                }
            )
        }

        composable(Screen.Review.route) {
            ReviewScreen(
                navController, viewModel
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController, settingsViewModel, viewModel, onBack = { navController.popBackStack() })
        }

        composable(
            route = "browser/{url}",
            arguments = listOf(navArgument("url") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            val url = Uri.decode(backStackEntry.arguments?.getString("url").orEmpty())
            BrowserScreen(url = url, onBack = { navController.popBackStack() }, viewModel = viewModel)
        }
    }
}