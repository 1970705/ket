package com.wordland.ui.uistate

/**
 * UI State for Audio Settings Screen
 */
sealed class AudioSettingsUiState {
    /**
     * Ready state with audio settings
     */
    data class Ready(
        val backgroundMusicEnabled: Boolean = true,
        val backgroundMusicVolume: Float = 0.7f,
        val soundEffectsEnabled: Boolean = true,
        val soundEffectsVolume: Float = 0.8f,
        val ttsEnabled: Boolean = true,
        val ttsLanguage: TtsLanguage = TtsLanguage.ENGLISH_US,
    ) : AudioSettingsUiState()

    /**
     * Loading state
     */
    data object Loading : AudioSettingsUiState()

    /**
     * Error state
     */
    data class Error(
        val message: String,
    ) : AudioSettingsUiState()
}

/**
 * Supported TTS languages
 */
enum class TtsLanguage(
    val displayName: String,
    val localeCode: String,
) {
    ENGLISH_US("英语 (美)", "en-US"),
    ENGLISH_UK("英语 (英)", "en-GB"),
    CHINESE("中文", "zh-CN"),
    ;

    companion object {
        fun fromLocaleCode(code: String): TtsLanguage {
            return values().find { it.localeCode == code } ?: ENGLISH_US
        }
    }
}
