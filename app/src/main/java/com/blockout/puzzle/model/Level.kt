package com.blockout.puzzle.model

data class Level(
    val id: Int,
    val name: String,
    val description: String = "",
    val difficulty: String = "NORMAL", // EASY, NORMAL, ADEPT, HARD, SUPER HARD
    val world: Int = 1,
    val gridWidth: Int = 6,
    val gridHeight: Int = 6,
    val blocks: List<Block>,
    val doors: List<ExitDoor>,
    val obstacles: List<Obstacle> = emptyList(),
    val maxMoves: Int = 30,
    val parMoves: Int = 20, // To get 3 stars
    val timeSeconds: Int = 180 // 3 minutes default
)

