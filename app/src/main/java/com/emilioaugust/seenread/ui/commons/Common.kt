package com.emilioaugust.seenread.ui.commons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.emilioaugust.seenread.R
import com.emilioaugust.seenread.ui.enums.Screen

@Composable
fun TextButtonCard(text: String, containerColor: Color = Color.Transparent,
                   contentColor: Color, onClick: () -> Unit) {
    TextButton(
        onClick = { onClick() },
        colors = ButtonDefaults.textButtonColors(
            containerColor = containerColor ,
            contentColor = contentColor
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 32.dp, end = 32.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
    }
}

@Composable
fun TopBarMenu(
    onArchiveClick: () -> Unit,
    onFavoritesClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
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
            text = { Text(stringResource(R.string.archive),
                color = MaterialTheme.colorScheme.onPrimaryContainer) },
            leadingIcon = {
                Icon(
                    Icons.Default.Archive,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) },
            onClick = {
                expanded = false
                onArchiveClick()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.favorites),
                color = MaterialTheme.colorScheme.onPrimaryContainer) },
            leadingIcon = {
                Icon(
                    Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) },
            onClick = {
                expanded = false
                onFavoritesClick()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.settings),
                color = MaterialTheme.colorScheme.onPrimaryContainer) },
            leadingIcon = {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) },
            onClick = {
                expanded = false
                onSettingsClick()
            }
        )
    }
}

@Composable
fun BottomBarNavigation(navController: NavController) {
    Column {
        Divider(
            color = MaterialTheme.colorScheme.outline,
            thickness = 0.8.dp
        )
        NavigationBar(containerColor = MaterialTheme.colorScheme.tertiaryContainer) {
            val currentRoute =
                navController.currentBackStackEntryAsState().value?.destination?.route
            NavigationBarItem(
                selected = currentRoute == Screen.Recent.route,
                onClick = {
                    navController.navigate(Screen.Recent.route) {
                        launchSingleTop = true
                    }
                },
                icon = { Icon(painterResource(R.drawable.recent), contentDescription = null,
                    modifier = Modifier.size(24.dp), tint = if(currentRoute == Screen.Recent.route) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }) },
                label = { Text(stringResource(R.string.recent)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onTertiary,
                    unselectedTextColor = MaterialTheme.colorScheme.onTertiary,
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentRoute == Screen.Library.route,
                onClick = {
                    navController.navigate(Screen.Library.route) {
                        launchSingleTop = true
                    }
                },
                icon = { Icon(painterResource(R.drawable.library), contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if(currentRoute == Screen.Library.route) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }
                ) },
                label = { Text(stringResource(R.string.library)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.Unspecified,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onTertiary,
                    unselectedTextColor = MaterialTheme.colorScheme.onTertiary,
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentRoute == Screen.Review.route,
                onClick = {
                    navController.navigate(Screen.Review.route) {
                        launchSingleTop = true
                    }
                },
                icon = { Icon(painterResource(R.drawable.review), contentDescription = null,
                    modifier = Modifier.size(24.dp), tint = if(currentRoute == Screen.Review.route) {
                        Color.Unspecified
                    } else {
                        MaterialTheme.colorScheme.onTertiary
                    }) },
                label = { Text(stringResource(R.string.review)) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onTertiary,
                    unselectedTextColor = MaterialTheme.colorScheme.onTertiary,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
fun MoreOnCard(expanded: Boolean,
               onDismiss: () -> Unit,
               onEdit: () -> Unit, onFavorite: () -> Unit,
               onArchive: () -> Unit, onDelete: () -> Unit,
               isFavorite: Boolean, isArchived: Boolean) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.edit), color = MaterialTheme.colorScheme.onPrimaryContainer) },
            leadingIcon = {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                ) },
            onClick = {
                onDismiss()
                onEdit()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (!isFavorite) {
                        stringResource(R.string.add_to_favorites)
                    } else {
                        stringResource(R.string.remove_from_favorites)
                    }, color = MaterialTheme.colorScheme.onPrimaryContainer
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
                )
                          },
            onClick = {
                onDismiss()
                onFavorite()
            }
        )
        DropdownMenuItem(
            text = {
                Text(
                    if (isArchived) {
                        stringResource(R.string.remove_from_archives)
                    } else {
                        stringResource(R.string.archive)
                    }, color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            leadingIcon = {
                Icon(
                    if (isArchived) {
                        Icons.Default.Unarchive
                    } else {
                        Icons.Default.Archive
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                onDismiss()
                onArchive()
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.delete), color = Color(0xFFF72F35)) },
            leadingIcon = {
                Icon(
                    Icons.Default.DeleteOutline,
                    contentDescription = null,
                    tint = Color(0xFFF72F35)
                )
            },
            onClick = {
                onDismiss()
                onDelete()
            }
        )
    }
}

@Composable
fun MoreOnArchiveCard(expanded: Boolean,
               onDismiss: () -> Unit,
               onArchive: () -> Unit, isArchived: Boolean) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onDismiss() },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium
    ) {
        DropdownMenuItem(
            text = {
                Text(
                    if (isArchived) {
                        stringResource(R.string.remove_from_archives)
                    } else {
                        stringResource(R.string.archive)
                    }, color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            },
            leadingIcon = {
                Icon(
                    if (isArchived) {
                        Icons.Default.Unarchive
                    } else {
                        Icons.Default.Archive
                    },
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            onClick = {
                onDismiss()
                onArchive()
            }
        )
    }
}

@Composable
fun DeleteAlertDialog(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        )
    ) {
        Column(modifier = Modifier
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.shapes.large
            )
            .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))

            Text(text = dialogTitle, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.height(8.dp))

            Text(text = dialogText, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = Color.LightGray,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    shape = MaterialTheme.shapes.large,
                    onClick = {
                        onDismissRequest()
                    }
                ) {
                    Text(text = stringResource(R.string.no_keep), style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp,
                            top = 10.dp, bottom = 10.dp)
                    )
                }

                Spacer(Modifier.width(12.dp))

                TextButton(
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = MaterialTheme.shapes.large,
                    onClick = {
                        onConfirmation()
                    },
                ) {
                    Text(text = stringResource(R.string.yes_delete), style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(start = 18.dp, end = 18.dp,
                            top = 10.dp, bottom = 10.dp)
                    )
                }
            }
        }
    }
}