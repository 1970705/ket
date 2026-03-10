# Audio Implementation Guide

**Version**: 1.0
**Last Updated**: 2026-02-18
**Author**: education-specialist

---

## Overview

This guide provides implementation instructions for integrating audio resources into the Wordland Android application.

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Audio Architecture](#audio-architecture)
3. [MediaPlayer vs ExoPlayer](#mediaplayer-vs-exoplayer)
4. [Implementation Steps](#implementation-steps)
5. [Code Examples](#code-examples)
6. [Volume Management](#volume-management)
7. [COPPA Compliance](#coppa-compliance)

---

## Quick Start

### Minimal Integration

```kotlin
// 1. Add audio files to app/src/main/res/raw/
// 2. Play a sound
val mediaPlayer = MediaPlayer.create(context, R.raw.word_look_001)
mediaPlayer.start()
mediaPlayer.release()
```

---

## Audio Architecture

### Component Design

```
┌─────────────────────────────────────────────────────────┐
│                      UI Layer                           │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │LearningScreen│  │HintCard      │  │LevelComplete │  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘  │
│         │                  │                  │          │
└─────────┼──────────────────┼──────────────────┼──────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                    Domain Layer                          │
│  ┌──────────────────────────────────────────────────┐  │
│  │              AudioManager (UseCase)              │  │
│  │  - playPronunciation(wordId: String)            │  │
│  │  - playSoundEffect(effect: SoundEffect)         │  │
│  │  - setVolume(level: Float)                      │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
          │                  │                  │
          ▼                  ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                     Data Layer                           │
│  ┌──────────────────────────────────────────────────┐  │
│  │            AudioRepository                        │  │
│  │  - getPronunciationResource(wordId): Int        │  │
│  │  - getSoundEffectResource(effect): Int          │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────────────────────────────────────────┐
│                   Android Framework                     │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐  │
│  │MediaPlayer   │  │SoundPool     │  │AudioManager  │  │
│  └──────────────┘  └──────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## MediaPlayer vs ExoPlayer

### Comparison

| Feature | MediaPlayer | ExoPlayer | SoundPool |
|---------|-------------|-----------|-----------|
| Complexity | Simple | Complex | Simple |
| Audio Length | Any duration | Any duration | Short (<1s) recommended |
| Overhead | Medium | High | Low |
| Use Case | Pronunciation, BGM | Advanced features | Sound effects |
| Recommendation | ✅ Use for MVP | ⚠️ Overkill for basic audio | ✅ Use for SFX |

### Recommendation

For Wordland MVP:
- **MediaPlayer**: For pronunciation and background music
- **SoundPool**: For sound effects (better performance for short, frequent sounds)

---

## Implementation Steps

### Step 1: Add Audio Resources

```bash
# Place audio files in:
app/src/main/res/raw/
```

### Step 2: Create Audio Data Models

```kotlin
// domain/model/AudioResource.kt

sealed class AudioResource {
    abstract val resourceId: Int
    abstract val volume: Float // 0.0 to 1.0
}

data class PronunciationAudio(
    val wordId: String,
    override val resourceId: Int,
    override val volume: Float = 1.0f
) : AudioResource()

enum class SoundEffect(val resourceName: String, val volume: Float = 0.85f) {
    CORRECT("sfx_correct"),
    INCORRECT("sfx_incorrect"),
    ACHIEVEMENT("sfx_achievement"),
    CLICK("sfx_click"),
    COMBO_3("sfx_combo_3"),
    COMBO_5("sfx_combo_5"),
    COMBO_10("sfx_combo_10"),
    LEVEL_COMPLETE("sfx_level_complete"),
    STAR("sfx_star")
}

enum class BackgroundMusic(val resourceName: String, val volume: Float = 0.35f) {
    MENU("bgm_menu"),
    GAMEPLAY("bgm_gameplay"),
    RELAX("bgm_relax")
}
```

### Step 3: Create Audio Repository

```kotlin
// data/repository/AudioRepository.kt

class AudioRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getPronunciationResource(wordId: String): Int {
        val resourceName = when (wordId) {
            "look_001" -> R.raw.word_look_001
            "see" -> R.raw.word_see
            "watch" -> R.raw.word_watch
            // ... map all 30 words
            else -> R.raw.word_look_001 // default
        }
        return resourceName
    }

    fun getSoundEffectResource(effect: SoundEffect): Int {
        val resourceName = effect.resourceName
        return context.resources.getIdentifier(
            resourceName,
            "raw",
            context.packageName
        )
    }

    fun getBackgroundMusicResource(music: BackgroundMusic): Int {
        val resourceName = music.resourceName
        return context.resources.getIdentifier(
            resourceName,
            "raw",
            context.packageName
        )
    }
}
```

### Step 4: Create AudioManager UseCase

```kotlin
// domain/usecase/usecases/PlayPronunciationUseCase.kt

class PlayPronunciationUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    operator fun invoke(wordId: String) {
        val resourceId = audioRepository.getPronunciationResource(wordId)
        // Implementation handled by AudioManager
    }
}

// domain/audiomanager/AudioManager.kt

@Singleton
class AudioManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRepository: AudioRepository
) {
    private var pronunciationPlayer: MediaPlayer? = null
    private val soundPool: SoundPool by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            SoundPool.Builder()
                .setMaxStreams(10)
                .setAudioAttributes(audioAttributes)
                .build()
        } else {
            @Suppress("DEPRECATION")
            SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        }
    }

    private var sfxLoaded = false
    private val sfxMap = mutableMapOf<SoundEffect, Int>()

    fun loadSoundEffects() {
        SoundEffect.values().forEach { effect ->
            val resourceId = audioRepository.getSoundEffectResource(effect)
            val soundId = soundPool.load(context, resourceId, 1)
            sfxMap[effect] = soundId
        }
        soundPool.setOnLoadCompleteListener { _, _, _ ->
            sfxLoaded = true
        }
    }

    /**
     * Play word pronunciation
     */
    fun playPronunciation(wordId: String) {
        releasePronunciationPlayer()

        val resourceId = audioRepository.getPronunciationResource(wordId)
        pronunciationPlayer = MediaPlayer.create(context, resourceId)?.apply {
            setOnCompletionListener { release() }
            start()
        }
    }

    /**
     * Play a sound effect
     */
    fun playSoundEffect(effect: SoundEffect) {
        if (!sfxLoaded) return

        sfxMap[effect]?.let { soundId ->
            soundPool.play(
                soundId,
                effect.volume, // left volume
                effect.volume, // right volume
                1,             // priority
                0,             // loop (0 = no loop)
                1.0f           // playback rate
            )
        }
    }

    /**
     * Play combo sound effect
     */
    fun playCombo(comboCount: Int) {
        val effect = when {
            comboCount >= 10 -> SoundEffect.COMBO_10
            comboCount >= 5 -> SoundEffect.COMBO_5
            comboCount >= 3 -> SoundEffect.COMBO_3
            else -> return
        }
        playSoundEffect(effect)
    }

    private fun releasePronunciationPlayer() {
        pronunciationPlayer?.release()
        pronunciationPlayer = null
    }

    fun release() {
        releasePronunciationPlayer()
        soundPool.release()
    }
}
```

### Step 5: Integrate with ViewModels

```kotlin
// ui/viewmodel/LearningViewModel.kt

@HiltViewModel
class LearningViewModel @Inject constructor(
    // ... existing dependencies
    private val audioManager: AudioManager
) : ViewModel() {

    fun playWordPronunciation() {
        val wordId = currentState.question?.wordId ?: return
        audioManager.playPronunciation(wordId)
    }

    fun submitAnswer(userAnswer: String) {
        // ... existing logic

        when (result.isCorrect) {
            true -> audioManager.playSoundEffect(SoundEffect.CORRECT)
            false -> audioManager.playSoundEffect(SoundEffect.INCORRECT)
        }

        // Play combo sound if applicable
        if (result.comboCount >= 3) {
            audioManager.playCombo(result.comboCount)
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Don't release AudioManager here as it's a singleton
    }
}
```

### Step 6: Add UI Components

```kotlin
// ui/components/PronunciationButton.kt

@Composable
fun PronunciationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_volume_up),
            contentDescription = "Play pronunciation",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

// Usage in LearningScreen
@Composable
fun LearningScreen(
    levelId: String,
    islandId: String,
    onNavigateBack: () -> Unit,
    viewModel: LearningViewModel = viewModel(factory = AppServiceLocator.provideFactory())
) {
    val uiState by viewModel.uiState.collectAsState()

    // ... existing UI

    // Add pronunciation button
    PronunciationButton(
        onClick = { viewModel.playWordPronunciation() }
    )
}
```

---

## Volume Management

### Settings Implementation

```kotlin
// data/preferences/AudioPreferences.kt

@Singleton
class AudioPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("audio_settings", Context.MODE_PRIVATE)

    var pronunciationVolume: Float
        get() = prefs.getFloat("pronunciation_volume", 1.0f)
        set(value) = prefs.edit().putFloat("pronunciation_volume", value).apply()

    var soundEffectsVolume: Float
        get() = prefs.getFloat("sfx_volume", 0.85f)
        set(value) = prefs.edit().putFloat("sfx_volume", value).apply()

    var musicVolume: Float
        get() = prefs.getFloat("music_volume", 0.35f)
        set(value) = prefs.edit().putFloat("music_volume", value).apply()

    var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()
}
```

### Settings Screen

```kotlin
// ui/screens/SettingsScreen.kt

@Composable
fun SettingsScreen(
    audioPreferences: AudioPreferences
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Sound toggle
        SwitchSetting(
            title = "Sound",
            checked = audioPreferences.isSoundEnabled,
            onCheckedChange = { audioPreferences.isSoundEnabled = it }
        )

        // Pronunciation volume
        SliderSetting(
            title = "Pronunciation Volume",
            value = audioPreferences.pronunciationVolume,
            onValueChange = { audioPreferences.pronunciationVolume = it }
        )

        // Sound effects volume
        SliderSetting(
            title = "Sound Effects Volume",
            value = audioPreferences.soundEffectsVolume,
            onValueChange = { audioPreferences.soundEffectsVolume = it }
        )

        // Music volume
        SliderSetting(
            title = "Music Volume",
            value = audioPreferences.musicVolume,
            onValueChange = { audioPreferences.musicVolume = it }
        )
    }
}
```

---

## Background Music Implementation

### BackgroundMusicManager

```kotlin
// domain/audiomanager/BackgroundMusicManager.kt

@Singleton
class BackgroundMusicManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRepository: AudioRepository,
    private val audioPreferences: AudioPreferences
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentMusic: BackgroundMusic? = null

    fun play(music: BackgroundMusic, loop: Boolean = true) {
        if (!audioPreferences.isSoundEnabled) return

        stop()

        val resourceId = audioRepository.getBackgroundMusicResource(music)
        mediaPlayer = MediaPlayer.create(context, resourceId)?.apply {
            isLooping = loop
            setVolume(
                audioPreferences.musicVolume,
                audioPreferences.musicVolume
            )
            start()
        }
        currentMusic = music
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
        currentMusic = null
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        if (!audioPreferences.isSoundEnabled) return
        mediaPlayer?.start()
    }

    fun setVolume(volume: Float) {
        mediaPlayer?.setVolume(volume, volume)
    }
}
```

---

## Lifecycle Management

### Application-level Audio Manager

```kotlin
// WordlandApplication.kt

class WordlandApplication : Application() {
    lateinit var audioManager: AudioManager
        private set

    lateinit var backgroundMusicManager: BackgroundMusicManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize audio managers
        audioManager = AppServiceLocator.audioManager
        backgroundMusicManager = AppServiceLocator.backgroundMusicManager

        // Load sound effects
        audioManager.loadSoundEffects()
    }
}
```

### Activity Lifecycle Integration

```kotlin
// MainActivity.kt

class MainActivity : ComponentActivity() {

    override fun onPause() {
        super.onPause()
        // Pause background music when app goes to background
        (application as WordlandApplication)
            .backgroundMusicManager
            .pause()
    }

    override fun onResume() {
        super.onResume()
        // Resume background music when app comes to foreground
        (application as WordlandApplication)
            .backgroundMusicManager
            .resume()
    }
}
```

---

## COPPA Compliance

### Privacy Requirements

1. **No Voice Data Collection**: Do not record or store user voice
2. **No Biometric Analysis**: Audio is for playback only
3. **Educational Purpose Only**: Clearly state educational use in privacy policy

### Privacy Policy Language

```
Audio Features:
- Our app uses pre-recorded audio files for vocabulary pronunciation
- We do not collect, store, or transmit any voice recordings from users
- Audio features are for educational purposes only
- No microphone access is required for audio playback
```

---

## Testing

### Unit Tests

```kotlin
// test/domain/audiomanager/AudioManagerTest.kt

class AudioManagerTest {
    // Test pronunciation playback
    // Test sound effect playback
    // Test volume adjustments
    // Test resource loading
}
```

### Integration Tests

```kotlin
// test/ui/LearningScreenAudioTest.kt

class LearningScreenAudioTest {
    // Test pronunciation button plays correct audio
    // Test correct answer plays correct sound effect
    // Test combo sounds play at appropriate times
}
```

---

## Performance Considerations

### Optimization Tips

1. **Preload Sound Effects**: Load all SFX at app startup
2. **Release Resources**: Always release MediaPlayer when done
3. **Use SoundPool for SFX**: Better performance for short sounds
4. **Compress Audio**: Use appropriate bitrate to reduce APK size
5. **Consider Streaming**: For very long audio files

### Memory Management

```kotlin
// Always release in appropriate lifecycle
override fun onDestroy() {
    super.onDestroy()
    audioManager.release()
    backgroundMusicManager.stop()
}
```

---

## Troubleshooting

### Common Issues

| Issue | Solution |
|-------|----------|
| Audio not playing | Check resource ID is valid |
| Audio cuts off | Use async preparation for long audio |
| Crackling sound | Check audio format and sample rate |
| Out of memory | Release MediaPlayer properly |
| SoundPool errors | Increase max streams in SoundPool.Builder |

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-02-18 | Initial implementation guide |

---

## Next Steps

1. Add audio files to `app/src/main/res/raw/`
2. Implement `AudioRepository` with word ID mapping
3. Implement `AudioManager` with MediaPlayer and SoundPool
4. Add `PronunciationButton` to `LearningScreen`
5. Add sound effects to answer feedback
6. Implement volume settings in settings screen
7. Test on real device with actual audio files
