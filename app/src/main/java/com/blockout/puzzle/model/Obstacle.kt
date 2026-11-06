package com.blockout.puzzle.model

enum class ObstacleType {
    ROCK,
    LOCK,
    CRATE
}

data class Obstacle(
    val id: String,
    val x: Int,
    val y: Int,
    val width: Int = 1,
    val height: Int = 1,
    val type: ObstacleType = ObstacleType.ROCK,
    val isDestroyed: Boolean = false
) {
    fun occupies(checkX: Int, checkY: Int): Boolean {
        if (isDestroyed) return false
        return checkX in x until (x + width) && checkY in y until (y + height)
    }
}

