package com.blockout.puzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.blockout.puzzle.data.LevelRepository
import com.blockout.puzzle.data.PreferencesManager
import com.blockout.puzzle.model.GameProgress
import com.blockout.puzzle.sound.SoundManager
import com.blockout.puzzle.ui.components.SettingsDialog
import com.blockout.puzzle.ui.screens.GamePlayScreen
import com.blockout.puzzle.ui.screens.HomeScreen
import com.blockout.puzzle.ui.screens.LevelSelectScreen
import com.blockout.puzzle.ui.screens.MissionsScreen
import com.blockout.puzzle.ui.screens.ThemesScreen

sealed class Screen {
    data object Home : Screen()
    data class Game(val levelId: Int) : Screen()
    data object LevelSelect : Screen()
    data object Themes : Screen()
    data object Missions : Screen()
}

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager
    private lateinit var prefManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        soundManager = SoundManager(this)
        prefManager = PreferencesManager(this)

        val initialProgress = prefManager.loadProgress()
        soundManager.soundEnabled = initialProgress.soundEnabled
        soundManager.hapticEnabled = initialProgress.hapticEnabled

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF101226)
            ) {
                MainApp(
                    prefManager = prefManager,
                    soundManager = soundManager
                )
            }
        }
    }
}

@Composable
fun MainApp(
    prefManager: PreferencesManager,
    soundManager: SoundManager
) {
    var progress by remember { mutableStateOf(prefManager.loadProgress()) }
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    fun updateProgress(newProgress: GameProgress) {
        progress = newProgress
        prefManager.saveProgress(newProgress)
        soundManager.soundEnabled = newProgress.soundEnabled
        soundManager.hapticEnabled = newProgress.hapticEnabled
    }

    // Hardware back button navigation
    BackHandler(enabled = currentScreen !is Screen.Home) {
        currentScreen = Screen.Home
    }

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                progress = progress,
                onContinueLevel = { lvlId ->
                    soundManager.playClick()
                    currentScreen = Screen.Game(lvlId)
                },
                onDailyPuzzle = {
                    soundManager.playClick()
                    currentScreen = Screen.Game(9999)
                },
                onLevelSelect = {
                    soundManager.playClick()
                    currentScreen = Screen.LevelSelect
                },
                onThemes = {
                    soundManager.playClick()
                    currentScreen = Screen.Themes
                },
                onMissions = {
                    soundManager.playClick()
                    currentScreen = Screen.Missions
                },
                onSettings = {
                    soundManager.playClick()
                    showSettingsDialog = true
                }
            )
        }

        is Screen.Game -> {
            val level = if (screen.levelId == 9999) {
                LevelRepository.getDailyPuzzle()
            } else {
                LevelRepository.getLevel(screen.levelId)
            }

            GamePlayScreen(
                level = level,
                progress = progress,
                soundManager = soundManager,
                onLevelCompleted = { lvlId, stars, coins, score ->
                    val updatedStars = progress.starsPerLevel.toMutableMap()
                    val existingStars = updatedStars[lvlId] ?: 0
                    if (stars > existingStars) {
                        updatedStars[lvlId] = stars
                    }
                    val newMax = if (lvlId < 9999 && lvlId >= progress.unlockedLevelMax) {
                        lvlId + 1
                    } else progress.unlockedLevelMax

                    val updatedHighScores = progress.highScoresPerLevel.toMutableMap()
                    val prevHigh = updatedHighScores[lvlId] ?: 0
                    if (score > prevHigh) {
                        updatedHighScores[lvlId] = score
                    }

                    val updatedProgress = progress.copy(
                        currentLevelId = if (lvlId < 9999) lvlId + 1 else progress.currentLevelId,
                        unlockedLevelMax = newMax,
                        starsPerLevel = updatedStars,
                        totalStars = updatedStars.values.sum(),
                        totalCoins = progress.totalCoins + coins,
                        highScoresPerLevel = updatedHighScores,
                        totalScore = progress.totalScore + score
                    )
                    updateProgress(updatedProgress)
                },
                onProgressUpdated = { updateProgress(it) },
                onBackToHome = {
                    currentScreen = Screen.Home
                },
                onNextLevel = { nextId ->
                    currentScreen = Screen.Game(nextId)
                }
            )
        }

        is Screen.LevelSelect -> {
            LevelSelectScreen(
                progress = progress,
                onLevelSelected = { lvlId ->
                    soundManager.playClick()
                    currentScreen = Screen.Game(lvlId)
                },
                onBack = {
                    soundManager.playClick()
                    currentScreen = Screen.Home
                }
            )
        }

        is Screen.Themes -> {
            ThemesScreen(
                progress = progress,
                prefManager = prefManager,
                onProgressUpdated = { updateProgress(it) },
                onBack = {
                    soundManager.playClick()
                    currentScreen = Screen.Home
                }
            )
        }

        is Screen.Missions -> {
            MissionsScreen(
                progress = progress,
                prefManager = prefManager,
                onProgressUpdated = { updateProgress(it) },
                onBack = {
                    soundManager.playClick()
                    currentScreen = Screen.Home
                }
            )
        }
    }

    if (showSettingsDialog) {
        SettingsDialog(
            soundEnabled = soundManager.soundEnabled,
            musicEnabled = progress.musicEnabled,
            hapticEnabled = soundManager.hapticEnabled,
            onSoundToggle = {
                val updated = progress.copy(soundEnabled = it)
                updateProgress(updated)
            },
            onMusicToggle = {
                val updated = progress.copy(musicEnabled = it)
                updateProgress(updated)
            },
            onHapticToggle = {
                val updated = progress.copy(hapticEnabled = it)
                updateProgress(updated)
            },
            onDismiss = {
                showSettingsDialog = false
            }
        )
    }
}

