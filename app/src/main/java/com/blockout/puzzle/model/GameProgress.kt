package com.blockout.puzzle.model

data class GameProgress(
    val currentLevelId: Int = 1,
    val unlockedLevelMax: Int = 1,
    val starsPerLevel: Map<Int, Int> = emptyMap(),
    val highScoresPerLevel: Map<Int, Int> = emptyMap(),
    val totalScore: Int = 0,
    val totalCoins: Int = 200,
    val totalStars: Int = 0,
    val undoCount: Int = 5,
    val hintCount: Int = 3,
    val extraMovesCount: Int = 3,
    val hammerCount: Int = 3,
    val activeThemeId: String = "midnight_glow",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticEnabled: Boolean = true
)

data class ThemeItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Int,
    val isUnlocked: Boolean,
    val backgroundStartColorHex: Long,
    val backgroundEndColorHex: Long,
    val boardBgColorHex: Long,
    val boardBorderColorHex: Long
)

data class Mission(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int,
    val rewardCoins: Int,
    val isClaimed: Boolean
)

