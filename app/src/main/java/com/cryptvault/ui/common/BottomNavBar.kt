package com.cryptvault.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Password
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cryptvault.ui.nav.Route

private data class BottomItem(val route: String, val label: String, val icon: ImageVector, val outlined: ImageVector)

private val items = listOf(
    BottomItem(Route.VaultTab, "Vault", Icons.Rounded.Key, Icons.Outlined.Key),
    BottomItem(Route.GeneratorTab, "Generator", Icons.Rounded.AutoAwesome, Icons.Outlined.Tune),
    BottomItem(Route.SettingsTab, "Settings", Icons.Rounded.Settings, Icons.Outlined.Password),
)

@Composable
fun BottomNavBar(navController: NavController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    NavigationBar {
        items.forEach { item ->
            val selected = currentRoute?.let {
                backStackEntry?.destination?.hierarchy?.any { d -> d.route == item.route }
            } ?: false
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.icon else item.outlined,
                        contentDescription = item.label,
                    )
                },
                label = { Text(item.label) },
                alwaysShowLabel = true,
            )
        }
    }
}