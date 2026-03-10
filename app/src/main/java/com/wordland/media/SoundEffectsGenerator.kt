package com.wordland.media

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 使用 Android 系统 ToneGenerator 生成游戏音效
 *
 * 无需外部音频文件，所有音效由系统生成
 */
@Singleton
class SystemSoundEffectsGenerator
    @Inject
    constructor() {
        companion object {
            private const val TAG = "SystemSoundEffects"
            private const val STREAM_TYPE = AudioManager.STREAM_MUSIC
            private const val DEFAULT_VOLUME = 80
        }

        /**
         * 播放正确答案音效
         */
        fun playCorrectAnswer(volume: Int = DEFAULT_VOLUME) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(STREAM_TYPE, volume)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                    delay(150)
                    toneGen.release()
                    Log.d(TAG, "Correct answer sound played")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play correct sound", e)
                }
            }
        }

        /**
         * 播放错误答案音效
         */
        fun playWrongAnswer(volume: Int = DEFAULT_VOLUME) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(STREAM_TYPE, volume)
                    toneGen.startTone(ToneGenerator.TONE_PROP_NACK, 300)
                    delay(300)
                    toneGen.release()
                    Log.d(TAG, "Wrong answer sound played")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play wrong sound", e)
                }
            }
        }

        /**
         * 播放连击音效
         */
        fun playCombo(
            comboLevel: Int,
            volume: Int = DEFAULT_VOLUME,
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(STREAM_TYPE, volume)

                    // 根据连击等级选择音调和持续时间
                    val (toneType, duration) =
                        when {
                            comboLevel >= 10 -> ToneGenerator.TONE_CDMA_ABBR_ALERT to 200
                            comboLevel >= 5 -> ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT to 150
                            comboLevel >= 3 -> ToneGenerator.TONE_PROP_NACK to 150
                            else -> ToneGenerator.TONE_PROP_BEEP to 100
                        }

                    toneGen.startTone(toneType, duration)

                    // 如果连击等级高，添加额外的"升级"音效
                    if (comboLevel >= 3) {
                        delay(duration.toLong())
                        toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, duration / 2)
                        delay((duration / 2).toLong())
                    }

                    toneGen.release()
                    Log.d(TAG, "Combo sound played: level $comboLevel")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play combo sound", e)
                }
            }
        }

        /**
         * 播放关卡完成音效
         */
        fun playLevelComplete(volume: Int = DEFAULT_VOLUME) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(STREAM_TYPE, volume)

                    // 播放上升音调序列表示庆祝
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 150)
                    delay(150)

                    toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 150)
                    delay(150)

                    toneGen.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT, 200)
                    delay(200)

                    toneGen.startTone(ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 400)
                    delay(400)

                    toneGen.release()
                    Log.d(TAG, "Level complete sound finished")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play level complete sound", e)
                }
            }
        }

        /**
         * 播放按钮点击音效
         */
        fun playButtonClick(volume: Int = DEFAULT_VOLUME) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(STREAM_TYPE, volume)
                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
                    delay(50)
                    toneGen.release()
                    Log.d(TAG, "Button click sound played")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play button sound", e)
                }
            }
        }

        /**
         * 播放提示音效
         */
        fun playHint(volume: Int = DEFAULT_VOLUME) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val toneGen = ToneGenerator(STREAM_TYPE, volume)
                    toneGen.startTone(ToneGenerator.TONE_CDMA_SOFT_ERROR_LITE, 200)
                    delay(200)
                    toneGen.release()
                    Log.d(TAG, "Hint sound played")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to play hint sound", e)
                }
            }
        }
    }
