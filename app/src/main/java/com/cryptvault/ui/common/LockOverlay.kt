package com.cryptvault.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cryptvault.data.repository.AppLifecyclePhase
import com.cryptvault.data.repository.SessionRepository
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

@Composable
fun LockOverlay(
    onLocked: () -> Unit,
    content: @Composable () -> Unit,
) {
    val session: SessionRepository = koinInject()
    val unlocked by session.isUnlockedFlow.collectAsState(initial = session.isUnlocked)
    var backgroundedAt by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        session.appLifecycle.collect { phase ->
            when (phase) {
                AppLifecyclePhase.Background -> {
                    backgroundedAt = System.currentTimeMillis()
                }
                AppLifecyclePhase.Foreground -> {
                    val ts = backgroundedAt
                    if (ts != null) {
                        val elapsed = (System.currentTimeMillis() - ts) / 1000
                        if (elapsed >= session.autoLockSeconds()) {
                            session.lock()
                            onLocked()
                        }
                        backgroundedAt = null
                    }
                }
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        content()
        AnimatedVisibility(
            visible = !unlocked,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp),
                ) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp),
                    )
                    Text(
                        "Vault locked",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        "Unlock to continue",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}