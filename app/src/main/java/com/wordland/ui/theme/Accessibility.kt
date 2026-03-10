package com.wordland.ui.theme

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics

/**
 * Accessibility utilities and helpers
 *
 * Provides common semantic properties and helpers for
 * screen reader support (TalkBack) and accessibility.
 *
 * @since 1.6 (Epic #8.4)
 */

/**
 * Custom semantic properties for accessibility
 */
val HeadingLevel = SemanticsPropertyKey<Int>("HeadingLevel")

/**
 * Screen level (root) heading
 */
val Modifier.heading1: Modifier
    get() =
        semantics(mergeDescendants = true) {
            this[HeadingLevel] = 1
        }

/**
 * Section level heading
 */
val Modifier.heading2: Modifier
    get() =
        semantics(mergeDescendants = true) {
            this[HeadingLevel] = 2
        }

/**
 * Subsection heading
 */
val Modifier.heading3: Modifier
    get() =
        semantics(mergeDescendants = true) {
            this[HeadingLevel] = 3
        }

/**
 * Accessibility content description builders
 */
object ContentDescriptions {
    /**
     * Build a content description for a star rating
     */
    fun starRating(
        stars: Int,
        maxStars: Int = 3,
    ): String {
        return when (stars) {
            maxStars -> "完美！$stars 星"
            0 -> "未获得星级"
            else -> "$stars 星"
        }
    }

    /**
     * Build a content description for progress percentage
     */
    fun progress(
        percentage: Int,
        label: String = "进度",
    ): String {
        return "$label $percentage 百分比"
    }

    /**
     * Build a content description for a combo counter
     */
    fun combo(count: Int): String {
        return when {
            count >= 10 -> "$count 连击！太棒了！"
            count >= 5 -> "$count 连击！继续加油！"
            count > 0 -> "$count 连击"
            else -> "开始答题获得连击"
        }
    }

    /**
     * Build a content description for accuracy
     */
    fun accuracy(percentage: Int): String {
        return when {
            percentage >= 90 -> "准确率 $percentage 百分比，非常好！"
            percentage >= 70 -> "准确率 $percentage 百分比，还不错"
            percentage >= 50 -> "准确率 $percentage 百分比，继续努力"
            else -> "准确率 $percentage 百分比，需要多多练习"
        }
    }

    /**
     * Build a content description for time taken
     */
    fun timeTaken(seconds: Int): String {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return when {
            minutes > 0 -> "用时 $minutes 分 $remainingSeconds 秒"
            else -> "用时 $remainingSeconds 秒"
        }
    }

    /**
     * Build a content description for level completion
     */
    fun levelComplete(
        level: String,
        stars: Int,
    ): String {
        return "$level 关卡完成，获得 ${starRating(stars)} 星"
    }

    /**
     * Build a content description for island unlock
     */
    fun islandUnlocked(islandName: String): String {
        return "解锁新岛屿：$islandName"
    }

    /**
     * Build a content description for button state
     */
    fun buttonState(
        enabled: Boolean,
        text: String,
    ): String {
        return if (enabled) {
            text
        } else {
            "$text，当前不可用"
        }
    }

    /**
     * Build a content description for hint count
     */
    fun hintCount(
        used: Int,
        max: Int,
    ): String {
        return "已使用 $used 个提示，共 $max 个"
    }
}

/**
 * Screen reader announcements for important state changes
 */
object ScreenReaderAnnouncements {
    /**
     * Announcement for level start
     */
    fun levelStart(
        levelName: String,
        wordCount: Int,
    ): String = "开始 $levelName，共 $wordCount 个单词"

    /**
     * Announcement for correct answer
     */
    fun correct(): String = "正确！"

    /**
     * Announcement for incorrect answer
     */
    fun incorrect(): String = "错误，再试一次"

    /**
     * Announcement for level completion
     */
    fun levelComplete(stars: Int): String {
        return "关卡完成！${ContentDescriptions.starRating(stars)}"
    }

    /**
     * Announcement for hint revealed
     */
    fun hintRevealed(level: Int): String {
        val levelText =
            when (level) {
                1 -> "首字母"
                2 -> "前半部分"
                3 -> "部分字母"
                else -> "提示"
            }
        return "提示显示：$levelText"
    }
}

/**
 * Focus order utilities for screen reader navigation
 */
object FocusOrder {
    /**
     * Defines the natural reading order for screens
     *
     * Use this to ensure proper navigation order in screen readers
     */
    const val HEADER = 1
    const val TITLE = 2
    const val CONTENT = 3
    const val INTERACTIVE = 4
    const val NAVIGATION = 5
}
