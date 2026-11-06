package com.blockout.puzzle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.blockout.puzzle.data.LevelRepository
import com.blockout.puzzle.engine.GameEngine
import com.blockout.puzzle.model.GameProgress
import com.blockout.puzzle.model.Level
import com.blockout.puzzle.sound.SoundManager
import com.blockout.puzzle.ui.components.BoosterBar
import com.blockout.puzzle.ui.components.GameBoardCanvas
import com.blockout.puzzle.ui.components.GameOverDialog
import com.blockout.puzzle.ui.components.SettingsDialog
import com.blockout.puzzle.ui.components.TopBar
import com.blockout.puzzle.ui.components.WinDialog
import kotlinx.coroutines.delay

@Composable
fun GamePlayScreen(
    level: Level,
    progress: GameProgress,
    soundManager: SoundManager,
    onLevelCompleted: (levelId: Int, stars: Int, coins: Int, score: Int) -> Unit,
    onProgressUpdated: (GameProgress) -> Unit,
    onBackToHome: () -> Unit,
    onNextLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var currentProgress by remember(progress) { mutableStateOf(progress) }

    val engine = remember(level) {
        GameEngine(
            level = level,
            onBlockEscaped = { _, _ ->
                soundManager.playEscape()
                soundManager.vibrate(SoundManager.HapticType.SUCCESS)
            },
            onLevelWon = {
                soundManager.playWin()
                soundManager.vibrate(SoundManager.HapticType.SUCCESS)
            }
        )
    }

    var remainingSeconds by remember(level) { mutableIntStateOf(level.timeSeconds) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var engineVersion by remember { mutableIntStateOf(0) } // Trigger recomposition on state changes

    // Timer countdown
    LaunchedEffect(isTimerRunning, engine.isWon, engine.isGameOver) {
        while (isTimerRunning && !engine.isWon && !engine.isGameOver && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            if (remainingSeconds <= 0) {
                // Out of time
                soundManager.playBump()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFD6E1F6), Color(0xFFE8EEFA), Color(0xFFDCE5F7))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar
            TopBar(
                level = level,
                score = engine.currentScore,
                coins = currentProgress.totalCoins,
                remainingSeconds = remainingSeconds,
                movesMade = engine.movesMade,
                maxMoves = engine.maxMoves,
                starsEarned = engine.starsEarned,
                onBackClick = {
                    soundManager.playClick()
                    onBackToHome()
                },
                onRestartClick = {
                    soundManager.playClick()
                    onNextLevel(level.id)
                },
                onPauseClick = {
                    soundManager.playClick()
                    showSettingsDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Game Board Canvas
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                GameBoardCanvas(
                    engine = engine,
                    onMoveMade = {
                        soundManager.playSlide()
                        soundManager.vibrate(SoundManager.HapticType.LIGHT)
                        engineVersion++
                    },
                    onBlockEscaped = { _, _ ->
                        engineVersion++
                    }
                )
            }

            // Booster Bar
            BoosterBar(
                undoCount = currentProgress.undoCount,
                hintCount = currentProgress.hintCount,
                extraMovesCount = currentProgress.extraMovesCount,
                hammerCount = currentProgress.hammerCount,
                onUndoClick = {
                    if (currentProgress.undoCount > 0) {
                        val success = engine.undo()
                        if (success) {
                            soundManager.playClick()
                            val updated = currentProgress.copy(undoCount = currentProgress.undoCount - 1)
                            currentProgress = updated
                            onProgressUpdated(updated)
                            engineVersion++
                        }
                    }
                },
                onHintClick = {
                    if (currentProgress.hintCount > 0) {
                        val hint = engine.findHintMove()
                        if (hint != null) {
                            soundManager.playBooster()
                            soundManager.vibrate(SoundManager.HapticType.MEDIUM)
                            // Auto apply or highlight hint
                            engine.applyMove(hint.first.id, hint.second.first, hint.second.second)
                            val updated = currentProgress.copy(hintCount = currentProgress.hintCount - 1)
                            currentProgress = updated
                            onProgressUpdated(updated)
                            engineVersion++
                        }
                    }
                },
                onExtraMovesClick = {
                    if (currentProgress.extraMovesCount > 0) {
                        soundManager.playBooster()
                        engine.addExtraMoves(5)
                        val updated = currentProgress.copy(extraMovesCount = currentProgress.extraMovesCount - 1)
                        currentProgress = updated
                        onProgressUpdated(updated)
                        engineVersion++
                    }
                },
                onHammerClick = {
                    if (currentProgress.hammerCount > 0) {
                        val destroyed = engine.destroyAnyObstacle()
                        if (destroyed) {
                            soundManager.playHammer()
                            soundManager.vibrate(SoundManager.HapticType.EXPLODE)
                            val updated = currentProgress.copy(hammerCount = currentProgress.hammerCount - 1)
                            currentProgress = updated
                            onProgressUpdated(updated)
                            engineVersion++
                        }
                    }
                }
            )
        }

        // Win Dialog
        if (engine.isWon) {
            val earnedStars = engine.starsEarned
            val rewardCoins = 50 + earnedStars * 20
            val finalScore = engine.calculateFinalScoreWithTime(remainingSeconds)
            val prevHigh = currentProgress.highScoresPerLevel[level.id] ?: 0
            val isNewHighScore = finalScore > prevHigh

            WinDialog(
                stars = earnedStars,
                score = finalScore,
                isNewHighScore = isNewHighScore,
                coinsEarned = rewardCoins,
                movesMade = engine.movesMade,
                onNextLevel = {
                    onLevelCompleted(level.id, earnedStars, rewardCoins, finalScore)
                    val nextId = if (level.id >= 9999) 1 else level.id + 1
                    onNextLevel(nextId)
                },
                onReplay = {
                    onLevelCompleted(level.id, earnedStars, rewardCoins, finalScore)
                    onNextLevel(level.id)
                },
                onHome = {
                    onLevelCompleted(level.id, earnedStars, rewardCoins, finalScore)
                    onBackToHome()
                }
            )
        }

        // Game Over Dialog
        if (engine.isGameOver || remainingSeconds <= 0) {
            GameOverDialog(
                extraMovesAvailable = currentProgress.extraMovesCount,
                onUseExtraMoves = {
                    if (currentProgress.extraMovesCount > 0) {
                        soundManager.playBooster()
                        engine.addExtraMoves(5)
                        if (remainingSeconds <= 0) remainingSeconds = 60
                        val updated = currentProgress.copy(extraMovesCount = currentProgress.extraMovesCount - 1)
                        currentProgress = updated
                        onProgressUpdated(updated)
                        engineVersion++
                    }
                },
                onRestart = {
                    onNextLevel(level.id)
                },
                onHome = onBackToHome
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                soundEnabled = soundManager.soundEnabled,
                musicEnabled = currentProgress.musicEnabled,
                hapticEnabled = soundManager.hapticEnabled,
                onSoundToggle = {
                    soundManager.soundEnabled = it
                    val updated = currentProgress.copy(soundEnabled = it)
                    currentProgress = updated
                    onProgressUpdated(updated)
                },
                onMusicToggle = {
                    val updated = currentProgress.copy(musicEnabled = it)
                    currentProgress = updated
                    onProgressUpdated(updated)
                },
                onHapticToggle = {
                    soundManager.hapticEnabled = it
                    val updated = currentProgress.copy(hapticEnabled = it)
                    currentProgress = updated
                    onProgressUpdated(updated)
                },
                onDismiss = {
                    showSettingsDialog = false
                }
            )
        }
    }
}

