package com.cryptvault.ui.vault

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cryptvault.domain.model.VaultEntry
import com.cryptvault.ui.common.AuroraBackground
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultEntryDetailScreen(
    id: Long,
    onEdit: () -> Unit,
    onBack: () -> Unit,
    vm: VaultViewModel = koinViewModel(),
) {
    var entry by remember(id) { mutableStateOf<VaultEntry?>(null) }
    var revealed by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(id) {
        vm.load(id) { entry = it }
    }

    val current = entry
    val auroraColors = listOf(
        current?.category?.tint ?: MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(current?.title.orEmpty(), maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit, enabled = current != null) {
                        Icon(Icons.Outlined.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AuroraBackground(
                baseColor = auroraColors[0],
                accentColor = auroraColors[1],
            )
            if (current == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Loading…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                DetailContent(
                    entry = current,
                    revealed = revealed,
                    onToggleReveal = { revealed = !revealed },
                    onCopy = {
                        vm.copyPassword(current)
                        scope.launch { snackbar.showSnackbar("Copied — clears in 30s") }
                    },
                )
            }
        }
    }
}

@Composable
private fun DetailContent(
    entry: VaultEntry,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        HeroCard(entry)
        UsernameCard(entry.username)
        PasswordCard(
            password = entry.password,
            revealed = revealed,
            onToggleReveal = onToggleReveal,
            onCopy = onCopy,
        )
        if (entry.notes.isNotBlank()) NotesCard(entry.notes)
        DetailRow(
            label = "Updated",
            value = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(entry.updatedAt)),
        )
        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun HeroCard(entry: VaultEntry) {
    val primary = MaterialTheme.colorScheme.primary
    val brush = remember(entry.category.tint, primary) {
        Brush.linearGradient(
            colors = listOf(
                entry.category.tint,
                entry.category.tint.copy(alpha = 0.7f),
                primary.copy(alpha = 0.6f),
            )
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        shadowElevation = 18.dp,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush)
                .padding(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        entry.category.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        entry.title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        entry.category.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )
                }
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun UsernameCard(username: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Username",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                username.ifBlank { "—" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PasswordCard(
    password: String,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
    onCopy: () -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 2.dp,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Password",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = onToggleReveal) {
                    Icon(
                        if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        contentDescription = if (revealed) "Hide" else "Reveal",
                    )
                }
            }
            AnimatedContent(
                targetState = Pair(revealed, password),
                transitionSpec = {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
                },
                label = "pwd",
            ) { (isRevealed, pwd) ->
                Text(
                    text = if (isRevealed) pwd else "•".repeat(pwd.length.coerceAtMost(24)),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                    ),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(Icons.Outlined.ContentCopy, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy")
                }
                OutlinedButton(
                    onClick = onToggleReveal,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Text(if (revealed) "Hide" else "Reveal")
                }
            }
        }
    }
}

@Composable
private fun NotesCard(notes: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                "Notes",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(notes, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}