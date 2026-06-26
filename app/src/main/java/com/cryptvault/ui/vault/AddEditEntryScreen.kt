package com.cryptvault.ui.vault

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.cryptvault.domain.model.EntryCategory
import com.cryptvault.domain.model.VaultEntry
import com.cryptvault.ui.common.AuroraBackground
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditEntryScreen(
    id: Long?,
    onDone: () -> Unit,
    onBack: () -> Unit,
    vm: VaultViewModel = koinViewModel(),
) {
    var title by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(EntryCategory.Login) }
    var showPassword by remember { mutableStateOf(false) }
    var loaded by remember(id) { mutableStateOf(id == null) }

    LaunchedEffect(id) {
        if (id != null) {
            vm.load(id) { entry ->
                if (entry != null) {
                    title = entry.title
                    username = entry.username
                    password = entry.password
                    notes = entry.notes
                    category = entry.category
                    loaded = true
                }
            }
        }
    }

    val canSave = title.isNotBlank() && password.isNotEmpty() && loaded
    val auroraColors = listOf(
        category.tint.copy(alpha = 0.85f),
        MaterialTheme.colorScheme.primary,
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (id == null) "New entry" else "Edit entry") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        },
        containerColor = Color.Transparent,
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            AuroraBackground(
                baseColor = auroraColors[0],
                accentColor = auroraColors[1],
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .imePadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                HeroCard(
                    title = title.ifBlank { "New entry" },
                    category = category,
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                ) {
                    items(EntryCategory.values().toList(), key = { it.name }) { cat ->
                        val isSel = cat == category
                        FilterChip(
                            selected = isSel,
                            onClick = { category = cat },
                            label = { Text(cat.label, fontWeight = FontWeight.Medium) },
                            leadingIcon = {
                                Icon(
                                    cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (isSel) MaterialTheme.colorScheme.onSecondaryContainer else cat.tint,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                selectedContainerColor = cat.tint.copy(alpha = 0.20f),
                                selectedLabelColor = cat.tint,
                            ),
                            shape = RoundedCornerShape(20.dp),
                        )
                    }
                }

                LabeledField(
                    label = "Title",
                    value = title,
                    onChange = { title = it },
                    placeholder = "e.g. GitHub",
                    leading = Icons.Outlined.VpnKey,
                    keyboardType = KeyboardType.Text,
                )

                LabeledField(
                    label = "Username or email",
                    value = username,
                    onChange = { username = it },
                    placeholder = "you@example.com",
                    leading = Icons.Outlined.Person,
                    keyboardType = KeyboardType.Email,
                )

                PasswordField(
                    value = password,
                    onChange = { password = it },
                    revealed = showPassword,
                    onToggleReveal = { showPassword = !showPassword },
                )

                NotesField(
                    value = notes,
                    onChange = { notes = it },
                )

                Button(
                    onClick = {
                        vm.save(
                            VaultEntry(
                                id = id ?: 0L,
                                title = title.trim(),
                                username = username.trim(),
                                password = password,
                                notes = notes.trim(),
                                category = category,
                                createdAt = 0L,
                                updatedAt = 0L,
                            ),
                            onDone,
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    AnimatedContent(
                        targetState = if (id == null) "Save entry" else "Update entry",
                        transitionSpec = {
                            (slideInVertically { it } + fadeIn())
                                .togetherWith(slideOutVertically { -it } + fadeOut())
                        },
                        label = "saveLabel",
                    ) { label ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Check, null)
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun HeroCard(title: String, category: EntryCategory) {
    val primary = MaterialTheme.colorScheme.primary
    val brush = remember(category.tint, primary) {
        Brush.linearGradient(
            colors = listOf(
                category.tint,
                category.tint.copy(alpha = 0.65f),
                primary.copy(alpha = 0.55f),
            )
        )
    }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(28.dp),
        tonalElevation = 4.dp,
        shadowElevation = 16.dp,
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
                        category.icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp),
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        category.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.85f),
                        fontWeight = FontWeight.Medium,
                    )
                }
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LabeledField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    placeholder: String,
    leading: ImageVector,
    keyboardType: KeyboardType,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        singleLine = true,
        leadingIcon = {
            Icon(leading, null, tint = MaterialTheme.colorScheme.primary)
        },
        shape = MaterialTheme.shapes.extraLarge,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordField(
    value: String,
    onChange: (String) -> Unit,
    revealed: Boolean,
    onToggleReveal: () -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Password") },
        singleLine = true,
        visualTransformation = if (revealed) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(Icons.Outlined.Lock, null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
            IconButton(onClick = onToggleReveal) {
                Icon(
                    if (revealed) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                    null,
                )
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesField(
    value: String,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text("Notes") },
        placeholder = { Text("Recovery codes, security questions, etc.") },
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        maxLines = 8,
    )
}