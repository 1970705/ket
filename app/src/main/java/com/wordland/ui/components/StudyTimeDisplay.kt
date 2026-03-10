package com.wordland.ui.components

/**
 * Helper object for formatting study time
 */
object StudyTimeDisplay {
    /**
     * Format study time in milliseconds to human-readable string
     * @param milliseconds Time in milliseconds
     * @return Formatted string (e.g., "2h 30m", "45m", "1h 5m")
     */
    fun format(milliseconds: Long): String {
        val totalMinutes = milliseconds / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}h${minutes}m"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "0m"
        }
    }

    /**
     * Format study time to detailed string
     * @param milliseconds Time in milliseconds
     * @return Formatted string (e.g., "2小时30分钟", "45分钟")
     */
    fun formatDetailed(milliseconds: Long): String {
        val totalMinutes = milliseconds / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
            hours > 0 -> "${hours}小时"
            minutes > 0 -> "${minutes}分钟"
            else -> "0分钟"
        }
    }

    /**
     * Format study time to short string (under 1 hour)
     * @param milliseconds Time in milliseconds
     * @return Formatted string (e.g., "150分钟")
     */
    fun formatShort(milliseconds: Long): String {
        val totalMinutes = milliseconds / 60000
        return "${totalMinutes}分钟"
    }

    /**
     * Get study time category for color coding
     * @param milliseconds Time in milliseconds
     * @return Category (BEGINNER, INTERMEDIATE, ADVANCED, EXPERT)
     */
    fun getCategory(milliseconds: Long): StudyTimeCategory {
        val totalHours = milliseconds / 3600000
        return when {
            totalHours < 1 -> StudyTimeCategory.BEGINNER
            totalHours < 10 -> StudyTimeCategory.INTERMEDIATE
            totalHours < 50 -> StudyTimeCategory.ADVANCED
            else -> StudyTimeCategory.EXPERT
        }
    }
}

/**
 * Study time category for gamification
 */
enum class StudyTimeCategory {
    BEGINNER, // < 1 hour
    INTERMEDIATE, // 1-10 hours
    ADVANCED, // 10-50 hours
    EXPERT, // 50+ hours
}

/**
 * Get display text for study time category
 */
fun StudyTimeCategory.getDisplayText(): String {
    return when (this) {
        StudyTimeCategory.BEGINNER -> "初学者"
        StudyTimeCategory.INTERMEDIATE -> "进阶中"
        StudyTimeCategory.ADVANCED -> "高手"
        StudyTimeCategory.EXPERT -> "大师"
    }
}

/**
 * Get emoji for study time category
 */
fun StudyTimeCategory.getEmoji(): String {
    return when (this) {
        StudyTimeCategory.BEGINNER -> "🌱"
        StudyTimeCategory.INTERMEDIATE -> "📚"
        StudyTimeCategory.ADVANCED -> "🎓"
        StudyTimeCategory.EXPERT -> "👑"
    }
}
