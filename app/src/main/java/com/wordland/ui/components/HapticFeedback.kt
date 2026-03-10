package com.wordland.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Haptic feedback manager for Wordland
 * Provides vibration feedback for game interactions
 */
@Singleton
class HapticFeedbackManager
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        private val vibrator: Vibrator? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

        /**
         * Light tap feedback for button presses
         */
        fun lightTap() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(10)
                }
            }
        }

        /**
         * Medium feedback for UI interactions
         */
        fun mediumTap() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(25)
                }
            }
        }

        /**
         * Heavy feedback for important events
         */
        fun heavyTap() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(50)
                }
            }
        }

        /**
         * Success feedback - double tap pattern
         */
        fun successPattern() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 30, 50, 30),
                            intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                            -1,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 30, 50, 30), -1)
                }
            }
        }

        /**
         * Error feedback - three short pulses
         */
        fun errorPattern() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 20, 30, 20, 30, 20),
                            intArrayOf(
                                0,
                                VibrationEffect.DEFAULT_AMPLITUDE,
                                0,
                                VibrationEffect.DEFAULT_AMPLITUDE,
                                0,
                                VibrationEffect.DEFAULT_AMPLITUDE,
                            ),
                            -1,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 20, 30, 20, 30, 20), -1)
                }
            }
        }

        /**
         * Star earned feedback - ascending pattern
         */
        fun starEarned() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(0, 15, 30, 25, 30, 35),
                            intArrayOf(0, 100, 0, 150, 0, 200),
                            -1,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(longArrayOf(0, 15, 30, 25, 30, 35), -1)
                }
            }
        }

        /**
         * Level complete celebration - longer pattern
         */
        fun levelComplete() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(
                                0,
                                30, 50, 30, 50, 30,
                                100, 50, 50, 50, 50,
                            ),
                            intArrayOf(
                                0, 150, 0, 180, 0, 200,
                                0, 255, 0, 255, 0, 255,
                            ),
                            -1,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(
                        longArrayOf(
                            0,
                            30, 50, 30, 50, 30,
                            100, 50, 50, 50, 50,
                        ),
                        -1,
                    )
                }
            }
        }

        /**
         * Island unlock celebration - long ascending then burst
         */
        fun islandUnlockCelebration() {
            if (hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(
                        VibrationEffect.createWaveform(
                            longArrayOf(
                                0,
                                20, 40, 20, 40, 20, 40,
                                20, 40, 20, 40,
                                200,
                            ),
                            intArrayOf(
                                0, 120, 0, 140, 0, 160, 0,
                                180, 0, 200, 0, 220,
                                0, 255,
                            ),
                            -1,
                        ),
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(
                        longArrayOf(
                            0,
                            20, 40, 20, 40, 20, 40,
                            20, 40, 20, 40,
                            200,
                        ),
                        -1,
                    )
                }
            }
        }

        /**
         * Cancel any ongoing vibration
         */
        fun cancel() {
            vibrator?.cancel()
        }

        /**
         * Check if device has vibrator
         */
        private fun hasVibrator(): Boolean {
            return vibrator?.hasVibrator() == true
        }

        /**
         * Check if amplitude control is supported
         */
        fun hasAmplitudeControl(): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.hasAmplitudeControl() == true
            } else {
                false
            }
        }
    }
