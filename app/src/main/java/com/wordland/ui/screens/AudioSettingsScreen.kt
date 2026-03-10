@file:OptIn(ExperimentalMaterial3Api::class)

package com.wordland.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordland.di.AppServiceLocator
import com.wordland.ui.components.WordlandButton
import com.wordland.ui.components.WordlandCard
import com.wordland.ui.components.WordlandOutlinedButton
import com.wordland.ui.uistate.AudioSettingsUiState
import com.wordland.ui.uistate.TtsLanguage
import com.wordland.ui.viewmodel.AudioSettingsViewModel

/**
 * Audio Settings Screen
 *
 * Allows users to configure audio settings:
 * - Background music (on/off, volume)
 * - Sound effects (on/off, volume)
 * - Text-to-Speech (on/off, language)
 */
@Composable
fun AudioSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AudioSettingsViewModel =
        viewModel(
            factory = AppServiceLocator.provideFactory(),
        ),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Initialize viewModel with factory if needed
    LaunchedEffect(Unit) {
        if (viewModel !is AudioSettingsViewModel) {
            // Factory initialization happens in ServiceLocator
        }
    }

    Scaffold(
        topBar = {
            AudioSettingsAppBar(onNavigateBack = onNavigateBack)
        },
    ) { paddingValues ->
        when (val state = uiState) {
            is AudioSettingsUiState.Loading -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is AudioSettingsUiState.Error -> {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        Text(
                            text = "⚠️",
                            style = MaterialTheme.typography.displayLarge,
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                        )
                        WordlandButton(
                            onClick = { viewModel.resetToDefault() },
                            text = "重试",
                        )
                    }
                }
            }

            is AudioSettingsUiState.Ready -> {
                AudioSettingsContent(
                    uiState = state,
                    onToggleBackgroundMusic = { viewModel.toggleBackgroundMusic(it) },
                    onBackgroundMusicVolumeChange = { viewModel.updateBackgroundMusicVolume(it) },
                    onToggleSoundEffects = { viewModel.toggleSoundEffects(it) },
                    onSoundEffectsVolumeChange = { viewModel.updateSoundEffectsVolume(it) },
                    onToggleTts = { viewModel.toggleTts(it) },
                    onTtsLanguageChange = { viewModel.updateTtsLanguage(it) },
                    onResetToDefault = { viewModel.resetToDefault() },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                )
            }
        }
    }
}

@Composable
private fun AudioSettingsAppBar(onNavigateBack: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = "🔊 音频设置",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "返回",
                )
            }
        },
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
    )
}

@Composable
private fun AudioSettingsContent(
    uiState: AudioSettingsUiState.Ready,
    onToggleBackgroundMusic: (Boolean) -> Unit,
    onBackgroundMusicVolumeChange: (Float) -> Unit,
    onToggleSoundEffects: (Boolean) -> Unit,
    onSoundEffectsVolumeChange: (Float) -> Unit,
    onToggleTts: (Boolean) -> Unit,
    onTtsLanguageChange: (TtsLanguage) -> Unit,
    onResetToDefault: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Background Music Section
        AudioSectionCard(
            icon = "🎵",
            title = "背景音乐",
            enabled = uiState.backgroundMusicEnabled,
            volume = uiState.backgroundMusicVolume,
            onEnabledChange = onToggleBackgroundMusic,
            onVolumeChange = onBackgroundMusicVolumeChange,
        )

        // Sound Effects Section
        AudioSectionCard(
            icon = "🔊",
            title = "音效",
            enabled = uiState.soundEffectsEnabled,
            volume = uiState.soundEffectsVolume,
            onEnabledChange = onToggleSoundEffects,
            onVolumeChange = onSoundEffectsVolumeChange,
        )

        // TTS Section
        TtsSectionCard(
            enabled = uiState.ttsEnabled,
            language = uiState.ttsLanguage,
            onEnabledChange = onToggleTts,
            onLanguageChange = onTtsLanguageChange,
        )

        // Action Buttons
        ActionButtons(onResetToDefault = onResetToDefault)
    }
}

@Composable
private fun AudioSectionCard(
    icon: String,
    title: String,
    enabled: Boolean,
    volume: Float,
    onEnabledChange: (Boolean) -> Unit,
    onVolumeChange: (Float) -> Unit,
) {
    WordlandCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon and Title
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = icon,
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }

            // Switch
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }

        if (enabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Volume Slider
            VolumeSlider(
                volume = volume,
                onVolumeChange = onVolumeChange,
            )
        }
    }
}

@Composable
private fun TtsSectionCard(
    enabled: Boolean,
    language: TtsLanguage,
    onEnabledChange: (Boolean) -> Unit,
    onLanguageChange: (TtsLanguage) -> Unit,
) {
    var showLanguageMenu by remember { mutableStateOf(false) }

    WordlandCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Icon and Title
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "🗣️",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Column {
                    Text(
                        text = "单词发音 (TTS)",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        text = "学习单词时朗读发音",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Switch
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }

        if (enabled) {
            Spacer(modifier = Modifier.height(16.dp))

            // Language Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "语种",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                OutlinedButton(
                    onClick = { showLanguageMenu = true },
                ) {
                    Text(language.displayName)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "选择语种",
                        modifier = Modifier.size(20.dp),
                    )
                }

                DropdownMenu(
                    expanded = showLanguageMenu,
                    onDismissRequest = { showLanguageMenu = false },
                ) {
                    TtsLanguage.entries.forEach { lang ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    lang.displayName,
                                    color =
                                        if (lang == language) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                )
                            },
                            onClick = {
                                onLanguageChange(lang)
                                showLanguageMenu = false
                            },
                            leadingIcon = {
                                if (lang == language) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeSlider(
    volume: Float,
    onVolumeChange: (Float) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "音量",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${(volume * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Slider(
            value = volume,
            onValueChange = onVolumeChange,
            valueRange = 0f..1f,
            steps = 9, // 10% increments (0%, 10%, ..., 100%)
        )
    }
}

@Composable
private fun ActionButtons(onResetToDefault: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        WordlandOutlinedButton(
            onClick = onResetToDefault,
            modifier = Modifier.weight(1f),
            text = "恢复默认",
        )
    }
}
