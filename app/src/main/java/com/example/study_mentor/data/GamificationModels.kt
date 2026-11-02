package com.example.study_mentor.data

/**
 * Data class representing RPG-style progression and gamification statistics.
 *
 * NOTE: "Level" strictly refers to an experience-based gaming progression level (RPG rank),
 * not an academic school grade.
 *
 * @param totalXp Cumulative experience points earned by the user across study activities.
 */
data class UserStats(
    val totalXp: Int = 0
)

/**
 * RPG Leveling Math and Title Calculations
 */
object GamificationMath {
    /**
     * Standard RPG formula: Each 100 XP grants 1 gaming level.
     * Level 1 starts at 0 XP.
     * Level 2 requires 100 XP, Level 3 requires 200 XP, etc.
     */
    const val XP_PER_LEVEL = 100

    /**
     * Calculates the gaming progression level based on total XP.
     * Formula: (totalXp / 100) + 1
     */
    fun calculateLevel(totalXp: Int): Int {
        val safeXp = maxOf(0, totalXp)
        return (safeXp / XP_PER_LEVEL) + 1
    }

    /**
     * Calculates the RPG gaming title corresponding to the player's level.
     */
    fun calculateTitle(level: Int): String {
        return when (level) {
            1 -> "Novice"
            2 -> "Scholar"
            3 -> "Adept"
            4 -> "Specialist"
            5 -> "Expert"
            6 -> "Master"
            7 -> "Grandmaster"
            8 -> "Champion"
            9 -> "Legend"
            else -> "Mythic"
        }
    }

    /**
     * Calculates XP accumulated within the current level (0 until XP_PER_LEVEL - 1).
     */
    fun calculateCurrentLevelXp(totalXp: Int): Int {
        val safeXp = maxOf(0, totalXp)
        return safeXp % XP_PER_LEVEL
    }

    /**
     * Calculates progress percentage towards the next level (0.0f to 1.0f).
     */
    fun calculateLevelProgress(totalXp: Int): Float {
        val currentLevelXp = calculateCurrentLevelXp(totalXp)
        return currentLevelXp.toFloat() / XP_PER_LEVEL.toFloat()
    }
}

// ─── Convenient Extension Properties for UserStats ──────────────────────────

/** The user's gaming progression level. */
val UserStats.level: Int
    get() = GamificationMath.calculateLevel(totalXp)

/** The RPG title matching the user's level. */
val UserStats.title: String
    get() = GamificationMath.calculateTitle(level)

/** XP earned within the current level. */
val UserStats.currentLevelXp: Int
    get() = GamificationMath.calculateCurrentLevelXp(totalXp)

/** Progress towards the next level as a normalized float (0.0f - 1.0f). */
val UserStats.levelProgress: Float
    get() = GamificationMath.calculateLevelProgress(totalXp)

/**
 * Formatted badge string combining XP, Level, and Title.
 * Example: "150 XP • Level 2 Scholar"
 */
val UserStats.badgeText: String
    get() = "$totalXp XP • Level $level $title"
