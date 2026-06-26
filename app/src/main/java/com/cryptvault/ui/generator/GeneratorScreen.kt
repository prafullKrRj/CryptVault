package com.cryptvault.ui.generator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cryptvault.ui.common.AuroraBackground
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneratorScreen(vm: GeneratorViewModel = koinViewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            vm.consumeMessage()
        }
    }

    val auroraColors = listOf(
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Generator", fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                OutputCard(
                    value = state.current,
                    onCopy = vm::copyCurrent,
                    onRegenerate = vm::regenerate,
                )

                StrengthCard(
                    entropy = state.strength.entropyBits,
                    score = state.strength.score,
                    label = state.strength.label,
                )

                LengthCard(
                    length = state.config.length,
                    onChange = vm::setLength,
                )

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Character classes",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                        ToggleRow("Uppercase A–Z", state.config.useUpper, vm::toggleUpper)
                        ToggleRow("Lowercase a–z", state.config.useLower, vm::toggleLower)
                        ToggleRow("Digits 0–9", state.config.useDigits, vm::toggleDigits)
                        ToggleRow("Symbols !@#…", state.config.useSymbols, vm::toggleSymbols)
                        ToggleRow("Exclude ambiguous (Il1O0…)", state.config.excludeAmbiguous, vm::toggleAmbiguous)
                    }
                }

                FilledTonalButton(
                    onClick = vm::regenerate,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Icon(Icons.Outlined.AutoAwesome, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Generate new password")
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@Composable
private fun OutputCard(value: String, onCopy: () -> Unit, onRegenerate: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 4.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(24.dp)) {
            Text(
                "Password",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                fontWeight = FontWeight.Medium,
            )
            Spacer(Modifier.height(12.dp))
            AnimatedContent(
                targetState = value,
                transitionSpec = {
                    (slideInVertically { it } + fadeIn())
                        .togetherWith(slideOutVertically { -it } + fadeOut())
                },
                label = "password",
            ) { text ->
                Text(
                    text = if (text.isEmpty()) "—" else text,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 22.sp,
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(Icons.Outlined.ContentCopy, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Copy")
                }
                SpinningRegenerateButton(onClick = onRegenerate, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SpinningRegenerateButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "spin")
    val spin by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "spin",
    )
    var pressed by remember { mutableStateOf(false) }
    LaunchedEffect(pressed) {
        if (pressed) {
            delay(160)
            pressed = false
        }
    }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh),
        label = "scale",
    )
    FilledTonalButton(
        onClick = {
            pressed = true
            onClick()
        },
        modifier = modifier.height(48.dp).graphicsLayer { scaleX = scale; scaleY = scale },
        shape = MaterialTheme.shapes.large,
    ) {
        Icon(Icons.Outlined.Refresh, null, modifier = Modifier.rotate(spin))
        Spacer(Modifier.width(8.dp))
        Text("Regenerate")
    }
}

@Composable
private fun StrengthCard(entropy: Double, score: Int, label: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Strength",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                AnimatedContent(
                    targetState = label,
                    transitionSpec = {
                        (slideInVertically { it } + fadeIn())
                            .togetherWith(slideOutVertically { -it } + fadeOut())
                    },
                    label = "label",
                ) { l ->
                    Text(
                        "$l • %.0f bits".format(entropy),
                        style = MaterialTheme.typography.titleSmall,
                        color = strengthColor(score),
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth().height(10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(5) { i ->
                    val active = i <= score
                    val a by animateFloatAsState(
                        targetValue = if (active) 1f else 0.18f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        label = "bar$i",
                    )
                    val color = strengthColor(i)
                    Box(
                        Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        color.copy(alpha = 0.35f + a * 0.55f),
                                        color.copy(alpha = 0.35f + a * 0.55f),
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun strengthColor(score: Int): Color = when {
    score <= 1 -> MaterialTheme.colorScheme.error
    score == 2 -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.primary
}

@Composable
private fun LengthCard(length: Int, onChange: (Int) -> Unit) {
    val animatedLength by animateFloatAsState(
        targetValue = length.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "length",
    )
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Length",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    animatedLength.toInt().toString(),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Slider(
                value = animatedLength,
                onValueChange = { onChange(it.toInt()) },
                valueRange = 4f..128f,
                steps = 0,
            )
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = { onChange() })
    }
}