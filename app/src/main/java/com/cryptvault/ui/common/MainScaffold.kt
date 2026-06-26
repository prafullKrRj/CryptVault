package com.cryptvault.ui.common

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cryptvault.ui.nav.Route

@Composable
fun MainScaffold(
    navController: NavController,
    vaultTab: @Composable (PaddingValues) -> Unit,
    generatorTab: @Composable (PaddingValues) -> Unit,
    settingsTab: @Composable (PaddingValues) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in setOf(Route.VaultTab, Route.GeneratorTab, Route.SettingsTab)) {
                BottomNavBar(navController)
            }
        }
    ) { padding ->
        when (currentRoute) {
            Route.VaultTab -> vaultTab(padding)
            Route.GeneratorTab -> generatorTab(padding)
            Route.SettingsTab -> settingsTab(padding)
        }
    }
}