package com.cryptvault.ui.vault

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cryptvault.domain.model.VaultEntry
import com.cryptvault.ui.common.AuroraBackground
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultListScreen(
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    vm: VaultViewModel = koinViewModel(),
) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val collapsed by remember {
        derivedStateOf { scrollBehavior.state.collapsedFraction > 0.5f }
    }
    val fabExtended by remember { derivedStateOf { !collapsed } }

    LaunchedEffect(state.transientMessage) {
        state.transientMessage?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val auroraColors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                ),
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                expanded = fabExtended,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Outlined.VpnKey, contentDescription = null) },
                text = { Text("Add") },
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
            Column(Modifier.fillMaxSize()) {
                SearchField(
                    query = state.query,
                    onChange = vm::onQueryChange,
                )

                AnimatedVisibility(
                    visible = !state.loading && state.entries.isEmpty(),
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut(),
                ) {
                    EmptyVault(Modifier.fillMaxSize())
                }

                AnimatedVisibility(
                    visible = state.entries.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.entries, key = { it.id }) { entry ->
                            StaggeredVaultRow(
                                entry = entry,
                                index = state.entries.indexOf(entry),
                                onClick = { onOpen(entry.id) },
                                onCopy = { vm.copyPassword(entry) },
                                onDelete = { vm.delete(entry) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder = { Text("Search vault") },
        leadingIcon = { Icon(Icons.Outlined.Search, null) },
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge,
    )
}

@Composable
private fun StaggeredVaultRow(
    entry: VaultEntry,
    index: Int,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    var visible by remember(entry.id) { mutableStateOf(false) }
    LaunchedEffect(entry.id) {
        delay(index * 35L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = tween(420, easing = LinearOutSlowInEasing),
            initialOffsetY = { it / 2 },
        ) + fadeIn(),
        exit = fadeOut(),
    ) {
        VaultListRow(
            entry = entry,
            onClick = onClick,
            onCopy = onCopy,
            onDelete = onDelete,
        )
    }
}

@Composable
private fun VaultListRow(
    entry: VaultEntry,
    onClick: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val tint = entry.category.tint
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = containerColor,
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 1.dp,
    ) {
        ListItem(
            headlineContent = {
                Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            },
            supportingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(tint),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        entry.category.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = tint,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "·",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (entry.username.isBlank()) "No username" else entry.username,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                }
            },
            leadingContent = { CategoryBadge(entry) },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AnimatedIconButton(
                        onClick = onCopy,
                        icon = Icons.Outlined.ContentCopy,
                        description = "Copy password",
                    )
                    AnimatedIconButton(
                        onClick = onDelete,
                        icon = Icons.Outlined.Delete,
                        description = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                    )
                }
            },
            colors = ListItemDefaults.colors(containerColor = containerColor),
            modifier = Modifier.padding(horizontal = 4.dp),
        )
    }
}

@Composable
private fun CategoryBadge(entry: VaultEntry) {
    val brush = remember(entry.category.tint) {
        Brush.linearGradient(
            colors = listOf(
                entry.category.tint,
                entry.category.tint.copy(alpha = 0.7f),
            )
        )
    }
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(brush),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            entry.category.icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
    }
}

@Composable
private fun AnimatedIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    description: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "press",
    )
    IconButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
    ) {
        Icon(icon, contentDescription = description, tint = tint)
        LaunchedEffect(pressed) {
            if (pressed) {
                delay(150)
                pressed = false
            }
        }
    }
}

@Composable
private fun EmptyVault(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Outlined.VpnKey,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(72.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Your vault is empty",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Tap the Add button to save your first entry.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}