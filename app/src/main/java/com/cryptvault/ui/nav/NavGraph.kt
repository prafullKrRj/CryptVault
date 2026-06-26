package com.cryptvault.ui.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cryptvault.data.repository.SessionRepository
import com.cryptvault.ui.common.BottomNavBar
import com.cryptvault.ui.generator.GeneratorScreen
import com.cryptvault.ui.settings.SettingsScreen
import com.cryptvault.ui.unlock.SetupMasterScreen
import com.cryptvault.ui.unlock.UnlockScreen
import com.cryptvault.ui.vault.AddEditEntryScreen
import com.cryptvault.ui.vault.VaultEntryDetailScreen
import com.cryptvault.ui.vault.VaultListScreen
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun CryptVaultNavGraph() {
    val nav = rememberNavController()
    val session: SessionRepository = koinInject()
    val isUnlocked by session.isUnlockedFlow.collectAsState()
    val isInitialized = session.isInitialized
    val scope = rememberCoroutineScope()
    val backStackEntry by nav.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomNav = currentRoute in setOf(Route.VaultTab, Route.GeneratorTab, Route.SettingsTab)

    val startDest = when {
        !isInitialized -> Route.Setup
        !isUnlocked -> Route.Unlock
        else -> Route.VaultTab
    }

    LaunchedEffect(isUnlocked, isInitialized) {
        when {
            !isInitialized && currentRoute != Route.Setup -> {
                nav.navigate(Route.Setup) { popUpTo(0); launchSingleTop = true }
            }
            isInitialized && !isUnlocked && currentRoute != Route.Unlock && currentRoute != Route.Setup -> {
                nav.navigate(Route.Unlock) { popUpTo(0); launchSingleTop = true }
            }
            isInitialized && isUnlocked && (currentRoute == Route.Unlock || currentRoute == Route.Setup) -> {
                nav.navigate(Route.VaultTab) { popUpTo(0); launchSingleTop = true }
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxSize()) {
            NavHost(navController = nav, startDestination = startDest) {
                composable(Route.Setup) {
                    SetupMasterScreen(
                        onDone = {
                            scope.launch {
                                session.markBiometricUnlocked()
                                nav.navigate(Route.VaultTab) { popUpTo(0) }
                            }
                        }
                    )
                }

                composable(Route.Unlock) {
                    UnlockScreen(
                        onUnlocked = {
                            nav.navigate(Route.VaultTab) { popUpTo(0) }
                        },
                        onForgotPassword = {
                            scope.launch {
                                session.resetAll()
                                nav.navigate(Route.Setup) { popUpTo(0); launchSingleTop = true }
                            }
                        },
                    )
                }

                composable(Route.VaultTab) {
                    VaultListScreen(
                        onAdd = { nav.navigate(Route.addEditEntry(null)) },
                        onOpen = { id -> nav.navigate(Route.entryDetail(id)) },
                    )
                }

                composable(Route.GeneratorTab) { GeneratorScreen() }
                composable(Route.SettingsTab) { SettingsScreen() }

                composable(
                    route = Route.EntryDetail,
                    arguments = listOf(navArgument("id") { type = NavType.LongType }),
                ) { stack ->
                    val id = stack.arguments?.getLong("id") ?: -1L
                    VaultEntryDetailScreen(
                        id = id,
                        onEdit = { nav.navigate(Route.addEditEntry(id)) },
                        onBack = { nav.popBackStack() },
                    )
                }

                composable(
                    route = Route.AddEditEntry,
                    arguments = listOf(navArgument("id") { type = NavType.LongType; defaultValue = -1L }),
                ) { stack ->
                    val id = stack.arguments?.getLong("id") ?: -1L
                    AddEditEntryScreen(
                        id = if (id == -1L) null else id,
                        onDone = { nav.popBackStack() },
                        onBack = { nav.popBackStack() },
                    )
                }
            }
        }
        if (showBottomNav) {
            BottomNavBar(nav)
        }
    }
}