package com.blockout.puzzle.model

data class Block(
    val id: String,
    val color: BlockColor,
    val x: Int,
    val y: Int,
    val width: Int, // span in columns
    val height: Int, // span in rows
    val direction: SlideDirection = if (width > height) SlideDirection.HORIZONTAL else if (height > width) SlideDirection.VERTICAL else SlideDirection.ALL,
    val symbol: String? = null, // e.g. "star", "diamond", "square", "cross", "key"
    val isEscaped: Boolean = false,
    val escapeProgress: Float = 0f, // 0f = on board, 1f = fully outside
    val escapeSide: ExitSide? = null
) {
    fun occupies(checkX: Int, checkY: Int): Boolean {
        if (isEscaped) return false
        return checkX in x until (x + width) && checkY in y until (y + height)
    }

    fun getAllOccupiedCells(): List<Pair<Int, Int>> {
        if (isEscaped) return emptyList()
        val cells = mutableListOf<Pair<Int, Int>>()
        for (col in x until (x + width)) {
            for (row in y until (y + height)) {
                cells.add(col to row)
            }
        }
        return cells
    }

    fun copyWithPosition(newX: Int, newY: Int): Block {
        return this.copy(x = newX, y = newY)
    }
}

