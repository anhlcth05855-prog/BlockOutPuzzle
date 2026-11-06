package com.blockout.puzzle.data

import android.content.Context
import android.content.SharedPreferences
import com.blockout.puzzle.model.GameProgress
import com.blockout.puzzle.model.Mission
import com.blockout.puzzle.model.ThemeItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("block_out_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_PROGRESS = "key_progress"
        private const val KEY_MISSIONS = "key_missions"
    }

    fun loadProgress(): GameProgress {
        val json = prefs.getString(KEY_PROGRESS, null) ?: return GameProgress()
        return try {
            gson.fromJson(json, GameProgress::class.java) ?: GameProgress()
        } catch (_: Exception) {
            GameProgress()
        }
    }

    fun saveProgress(progress: GameProgress) {
        val json = gson.toJson(progress)
        prefs.edit().putString(KEY_PROGRESS, json).apply()
    }

    fun loadMissions(): List<Mission> {
        val json = prefs.getString(KEY_MISSIONS, null)
        if (json != null) {
            try {
                val type = object : TypeToken<List<Mission>>() {}.type
                val list: List<Mission>? = gson.fromJson(json, type)
                if (!list.isNullOrEmpty()) return list
            } catch (_: Exception) {}
        }
        return getDefaultMissions()
    }

    fun saveMissions(missions: List<Mission>) {
        val json = gson.toJson(missions)
        prefs.edit().putString(KEY_MISSIONS, json).apply()
    }

    private fun getDefaultMissions(): List<Mission> = listOf(
        Mission("m1", "Puzzle Apprentice", "Solve 3 adventure puzzle levels", 3, 0, 50, false),
        Mission("m2", "Path Finder", "Solve 10 adventure puzzle levels", 10, 0, 150, false),
        Mission("m3", "Swift Thinker", "Win 3 levels using 5 moves or fewer", 3, 0, 80, false),
        Mission("m4", "Par Perfection", "Win 5 levels within par move threshold", 5, 0, 100, false),
        Mission("m5", "Starlight Collector", "Amass a total of 15 gold stars", 15, 0, 60, false),
        Mission("m6", "Daily Adventurer", "Complete today's Daily Challenge", 1, 0, 100, false)
    )

    fun getAvailableThemes(): List<ThemeItem> = listOf(
        ThemeItem(
            id = "midnight_glow",
            name = "Midnight Glow",
            description = "Sleek nighttime starry sky with maximum contrast",
            price = 0,
            isUnlocked = true,
            backgroundStartColorHex = 0xFF12132AL,
            backgroundEndColorHex = 0xFF1E1F42L,
            boardBgColorHex = 0xFF1C1E3DL,
            boardBorderColorHex = 0xFF2F3261L
        ),
        ThemeItem(
            id = "sunny_meadow",
            name = "Sunny Meadow",
            description = "Cheerful blue skies and spring green pastures",
            price = 100,
            isUnlocked = false,
            backgroundStartColorHex = 0xFF1E3A8AL,
            backgroundEndColorHex = 0xFF047857L,
            boardBgColorHex = 0xFF172554L,
            boardBorderColorHex = 0xFF1E40AFL
        ),
        ThemeItem(
            id = "candy_cloud",
            name = "Candy Cloud",
            description = "Dreamy cotton candy skies and pastel clouds",
            price = 150,
            isUnlocked = false,
            backgroundStartColorHex = 0xFF4A154BL,
            backgroundEndColorHex = 0xFF701A75L,
            boardBgColorHex = 0xFF3B0764L,
            boardBorderColorHex = 0xFF6B21A8L
        ),
        ThemeItem(
            id = "coral_sunset",
            name = "Coral Sunset",
            description = "Warm glowing sunset rays and desert horizons",
            price = 180,
            isUnlocked = false,
            backgroundStartColorHex = 0xFF431407L,
            backgroundEndColorHex = 0xFF9A3412L,
            boardBgColorHex = 0xFF2A0D05L,
            boardBorderColorHex = 0xFF7C2D12L
        ),
        ThemeItem(
            id = "underwater_world",
            name = "Underwater World",
            description = "Deep ocean luminescence with vibrant coral reefs",
            price = 200,
            isUnlocked = false,
            backgroundStartColorHex = 0xFF082F49L,
            backgroundEndColorHex = 0xFF0C4A6EL,
            boardBgColorHex = 0xFF041E33L,
            boardBorderColorHex = 0xFF0369A1L
        )
    )
}

