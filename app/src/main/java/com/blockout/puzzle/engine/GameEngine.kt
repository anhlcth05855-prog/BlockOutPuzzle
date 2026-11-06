package com.blockout.puzzle.engine

import com.blockout.puzzle.model.Block
import com.blockout.puzzle.model.BlockColor
import com.blockout.puzzle.model.ExitDoor
import com.blockout.puzzle.model.ExitSide
import com.blockout.puzzle.model.Level
import com.blockout.puzzle.model.Obstacle
import com.blockout.puzzle.model.SlideDirection
import kotlin.math.roundToInt

data class DragLimits(
    val minXOnBoard: Int,
    val maxXOnBoard: Int,
    val minYOnBoard: Int,
    val maxYOnBoard: Int,
    val canEscapeLeft: Boolean,
    val canEscapeRight: Boolean,
    val canEscapeTop: Boolean,
    val canEscapeBottom: Boolean
)

data class MoveResult(
    val blockId: String,
    val oldX: Int,
    val oldY: Int,
    val newX: Int,
    val newY: Int,
    val escaped: Boolean,
    val escapeSide: ExitSide? = null,
    val scoreEarned: Int = 0,
    val comboMultiplier: Int = 1
)

class GameEngine(
    val level: Level,
    var onBlockEscaped: ((Block, ExitSide) -> Unit)? = null,
    var onLevelWon: (() -> Unit)? = null
) {
    val gridWidth: Int = level.gridWidth
    val gridHeight: Int = level.gridHeight

    var blocks: List<Block>
        private set

    var doors: List<ExitDoor> = level.doors
        private set

    var obstacles: List<Obstacle> = level.obstacles.map { it.copy() }
        private set

    var movesMade: Int = 0
        private set

    var extraMovesAdded: Int = 0

    val maxMoves: Int
        get() = level.maxMoves + extraMovesAdded

    var isWon: Boolean = false
        private set

    var isGameOver: Boolean = false
        private set

    // Real-time score & combo system
    var currentScore: Int = 0
        private set

    var comboMultiplier: Int = 1
        private set

    var movesBonus: Int = 0
        private set

    var levelClearBonus: Int = 0
        private set

    private var lastEscapeTimeMs: Long = 0L
    private val COMBO_WINDOW_MS = 3800L

    private val undoStack = mutableListOf<List<Block>>()

    val remainingBlocks: Int
        get() = blocks.count { !it.isEscaped }

    val totalBlocks: Int
        get() = blocks.size

    val starsEarned: Int
        get() {
            if (!isWon) return 0
            return when {
                movesMade <= level.parMoves -> 3
                movesMade <= (level.parMoves + (maxMoves - level.parMoves) / 2) -> 2
                else -> 1
            }
        }

    init {
        // Enforce 100% overlap-free block positioning
        val occupied = mutableSetOf<Pair<Int, Int>>()
        for (obs in obstacles) {
            for (ox in obs.x until (obs.x + obs.width)) {
                for (oy in obs.y until (obs.y + obs.height)) {
                    occupied.add(ox to oy)
                }
            }
        }

        val sanitizedBlocks = mutableListOf<Block>()
        for (rawBlock in level.blocks) {
            val clampedX = rawBlock.x.coerceIn(0, (gridWidth - rawBlock.width).coerceAtLeast(0))
            val clampedY = rawBlock.y.coerceIn(0, (gridHeight - rawBlock.height).coerceAtLeast(0))
            var block = rawBlock.copy(x = clampedX, y = clampedY)

            val myCells = block.getAllOccupiedCells()
            if (myCells.any { it in occupied }) {
                // Relocate block to the nearest valid non-overlapping slot
                var placed = false
                for (r in 0..(gridHeight - block.height)) {
                    for (c in 0..(gridWidth - block.width)) {
                        val testCells = (c until (c + block.width)).flatMap { tc ->
                            (r until (r + block.height)).map { tr -> tc to tr }
                        }
                        if (testCells.none { it in occupied }) {
                            block = block.copy(x = c, y = r)
                            sanitizedBlocks.add(block)
                            occupied.addAll(testCells)
                            placed = true
                            break
                        }
                    }
                    if (placed) break
                }
                if (!placed) {
                    sanitizedBlocks.add(block)
                }
            } else {
                sanitizedBlocks.add(block)
                occupied.addAll(myCells)
            }
        }
        this.blocks = sanitizedBlocks
    }

    fun getBlock(id: String): Block? = blocks.find { it.id == id }

    fun calculateLimits(block: Block): DragLimits {
        if (block.isEscaped) {
            return DragLimits(block.x, block.x, block.y, block.y, false, false, false, false)
        }

        val curX = block.x.coerceIn(0, (gridWidth - block.width).coerceAtLeast(0))
        val curY = block.y.coerceIn(0, (gridHeight - block.height).coerceAtLeast(0))

        val otherBlocks = blocks.filter { it.id != block.id && !it.isEscaped }
        val activeObstacles = obstacles.filter { !it.isDestroyed }

        var minX = curX
        var maxX = curX
        var minY = curY
        var maxY = curY

        if (block.direction.canMoveHorizontal) {
            // Check sliding LEFT
            var testX = curX - 1
            while (testX >= 0) {
                if (isAreaBlocked(testX, curY, 1, block.height, otherBlocks, activeObstacles)) break
                minX = testX
                testX--
            }

            // Check sliding RIGHT
            testX = curX + 1
            while (testX + block.width <= gridWidth) {
                if (isAreaBlocked(testX + block.width - 1, curY, 1, block.height, otherBlocks, activeObstacles)) break
                maxX = testX
                testX++
            }
        }

        if (block.direction.canMoveVertical) {
            // Check sliding UP
            var testY = curY - 1
            while (testY >= 0) {
                if (isAreaBlocked(curX, testY, block.width, 1, otherBlocks, activeObstacles)) break
                minY = testY
                testY--
            }

            // Check sliding DOWN
            testY = curY + 1
            while (testY + block.height <= gridHeight) {
                if (isAreaBlocked(curX, testY + block.height - 1, block.width, 1, otherBlocks, activeObstacles)) break
                maxY = testY
                testY++
            }
        }

        // Can escape LEFT: path to left edge (col 0) is clear, and matching door exists covering these rows
        val canEscapeLeft = block.direction.canMoveHorizontal && (minX == 0) && (curY until (curY + block.height)).all { row ->
            doors.any { it.side == ExitSide.LEFT && it.color == block.color && row in it.index until (it.index + it.widthCells) }
        }

        // Can escape RIGHT: path to right edge is clear, and matching door exists covering these rows
        val canEscapeRight = block.direction.canMoveHorizontal && (maxX + block.width == gridWidth) && (curY until (curY + block.height)).all { row ->
            doors.any { it.side == ExitSide.RIGHT && it.color == block.color && row in it.index until (it.index + it.widthCells) }
        }

        // Can escape TOP: path to top edge (row 0) is clear, and matching door exists covering these columns
        val canEscapeTop = block.direction.canMoveVertical && (minY == 0) && (curX until (curX + block.width)).all { col ->
            doors.any { it.side == ExitSide.TOP && it.color == block.color && col in it.index until (it.index + it.widthCells) }
        }

        // Can escape BOTTOM: path to bottom edge is clear, and matching door exists covering these columns
        val canEscapeBottom = block.direction.canMoveVertical && (maxY + block.height == gridHeight) && (curX until (curX + block.width)).all { col ->
            doors.any { it.side == ExitSide.BOTTOM && it.color == block.color && col in it.index until (it.index + it.widthCells) }
        }

        return DragLimits(
            minXOnBoard = minX,
            maxXOnBoard = maxX,
            minYOnBoard = minY,
            maxYOnBoard = maxY,
            canEscapeLeft = canEscapeLeft,
            canEscapeRight = canEscapeRight,
            canEscapeTop = canEscapeTop,
            canEscapeBottom = canEscapeBottom
        )
    }

    private fun isAreaBlocked(
        startX: Int,
        startY: Int,
        width: Int,
        height: Int,
        otherBlocks: List<Block>,
        obstacles: List<Obstacle>
    ): Boolean {
        for (x in startX until (startX + width)) {
            for (y in startY until (startY + height)) {
                if (otherBlocks.any { it.occupies(x, y) }) return true
                if (obstacles.any { it.occupies(x, y) }) return true
            }
        }
        return false
    }

    /**
     * Applies a move based on drag delta (in grid units) or fling/swipe gesture.
     */
    fun applyMove(
        blockId: String,
        deltaCellsX: Float,
        deltaCellsY: Float,
        isFling: Boolean = false,
        forceEscape: Boolean = false
    ): MoveResult? {
        val block = getBlock(blockId) ?: return null
        if (block.isEscaped) return null

        val curX = block.x.coerceIn(0, (gridWidth - block.width).coerceAtLeast(0))
        val curY = block.y.coerceIn(0, (gridHeight - block.height).coerceAtLeast(0))

        val limits = calculateLimits(block)

        var newX = curX
        var newY = curY
        var escaped = false
        var escapeSide: ExitSide? = null

        // Sensitivity threshold for escape gesture
        val escapeSensitivity = 0.20f

        // Check horizontal movement
        if (block.direction.canMoveHorizontal) {
            when {
                // If forceEscape or user swiped / dragged left towards an open door
                (forceEscape || deltaCellsX <= -escapeSensitivity || isFling) && limits.canEscapeLeft && deltaCellsX < 0f -> {
                    escaped = true
                    escapeSide = ExitSide.LEFT
                    newX = -block.width
                }
                // If forceEscape or user swiped / dragged right towards an open door
                (forceEscape || deltaCellsX >= escapeSensitivity || isFling) && limits.canEscapeRight && deltaCellsX > 0f -> {
                    escaped = true
                    escapeSide = ExitSide.RIGHT
                    newX = gridWidth
                }
                isFling && deltaCellsX < 0f -> {
                    newX = limits.minXOnBoard
                }
                isFling && deltaCellsX > 0f -> {
                    newX = limits.maxXOnBoard
                }
                else -> {
                    val floatTargetX = curX + deltaCellsX
                    newX = floatTargetX.roundToInt().coerceIn(limits.minXOnBoard, limits.maxXOnBoard)
                }
            }
        }

        // Check vertical movement
        if (block.direction.canMoveVertical && !escaped) {
            when {
                // If forceEscape or user swiped / dragged UP towards an open door
                (forceEscape || deltaCellsY <= -escapeSensitivity || isFling) && limits.canEscapeTop && deltaCellsY < 0f -> {
                    escaped = true
                    escapeSide = ExitSide.TOP
                    newY = -block.height
                }
                // If forceEscape or user swiped / dragged DOWN towards an open door
                (forceEscape || deltaCellsY >= escapeSensitivity || isFling) && limits.canEscapeBottom && deltaCellsY > 0f -> {
                    escaped = true
                    escapeSide = ExitSide.BOTTOM
                    newY = gridHeight
                }
                isFling && deltaCellsY < 0f -> {
                    newY = limits.minYOnBoard
                }
                isFling && deltaCellsY > 0f -> {
                    newY = limits.maxYOnBoard
                }
                else -> {
                    val floatTargetY = curY + deltaCellsY
                    newY = floatTargetY.roundToInt().coerceIn(limits.minYOnBoard, limits.maxYOnBoard)
                }
            }
        }

        // If no movement happened
        if (!escaped && newX == curX && newY == curY) {
            if (block.x != curX || block.y != curY) {
                blocks = blocks.map { if (it.id == blockId) it.copy(x = curX, y = curY) else it }
            }
            return null
        }

        // Save state for undo
        undoStack.add(blocks.map { it.copy() })

        var scoreEarned = 0
        if (escaped) {
            val now = System.currentTimeMillis()
            if (now - lastEscapeTimeMs <= COMBO_WINDOW_MS && lastEscapeTimeMs > 0L) {
                comboMultiplier = (comboMultiplier + 1).coerceAtMost(5)
            } else {
                comboMultiplier = 1
            }
            lastEscapeTimeMs = now

            val baseBlockPts = block.width * block.height * 100
            scoreEarned = baseBlockPts * comboMultiplier
            currentScore += scoreEarned
        }

        val updatedBlock = block.copy(
            x = newX,
            y = newY,
            isEscaped = escaped,
            escapeSide = escapeSide
        )

        blocks = blocks.map { if (it.id == blockId) updatedBlock else it }
        movesMade++

        if (escaped && escapeSide != null) {
            onBlockEscaped?.invoke(updatedBlock, escapeSide)
        }

        checkWinState()

        return MoveResult(
            blockId = blockId,
            oldX = curX,
            oldY = curY,
            newX = newX,
            newY = newY,
            escaped = escaped,
            escapeSide = escapeSide,
            scoreEarned = scoreEarned,
            comboMultiplier = comboMultiplier
        )
    }

    /**
     * If block is tapped and has an immediate exit available, escape it directly!
     */
    fun tryQuickEscape(blockId: String): MoveResult? {
        val block = getBlock(blockId) ?: return null
        if (block.isEscaped) return null

        val limits = calculateLimits(block)

        if (limits.canEscapeLeft) {
            return applyMove(blockId, -1f, 0f, forceEscape = true)
        }
        if (limits.canEscapeRight) {
            return applyMove(blockId, 1f, 0f, forceEscape = true)
        }
        if (limits.canEscapeTop) {
            return applyMove(blockId, 0f, -1f, forceEscape = true)
        }
        if (limits.canEscapeBottom) {
            return applyMove(blockId, 0f, 1f, forceEscape = true)
        }

        return null
    }

    private fun checkWinState() {
        if (blocks.all { it.isEscaped }) {
            isWon = true
            movesBonus = (maxMoves - movesMade).coerceAtLeast(0) * 50
            levelClearBonus = level.id * 150
            currentScore += movesBonus + levelClearBonus
            onLevelWon?.invoke()
        } else if (movesMade >= maxMoves) {
            isGameOver = true
        }
    }

    fun calculateFinalScoreWithTime(remainingSeconds: Int): Int {
        val timeBonus = remainingSeconds * 10
        return currentScore + timeBonus
    }

    fun undo(): Boolean {
        if (undoStack.isEmpty()) return false
        blocks = undoStack.removeAt(undoStack.lastIndex)
        if (movesMade > 0) movesMade--
        comboMultiplier = 1
        isWon = false
        isGameOver = false
        return true
    }

    fun addExtraMoves(count: Int = 5) {
        extraMovesAdded += count
        if (isGameOver && movesMade < maxMoves) {
            isGameOver = false
        }
    }

    fun destroyObstacle(obstacleId: String): Boolean {
        val obstacle = obstacles.find { it.id == obstacleId } ?: return false
        if (obstacle.isDestroyed) return false
        obstacles = obstacles.map {
            if (it.id == obstacleId) it.copy(isDestroyed = true) else it
        }
        return true
    }

    fun destroyAnyObstacle(): Boolean {
        val firstActive = obstacles.find { !it.isDestroyed } ?: return false
        return destroyObstacle(firstActive.id)
    }

    fun findHintMove(): Pair<Block, Pair<Float, Float>>? {
        // 1. Look for any block that can immediately escape
        for (b in blocks.filter { !it.isEscaped }) {
            val limits = calculateLimits(b)
            if (limits.canEscapeLeft) return b to ((-b.width - b.x - 1).toFloat() to 0f)
            if (limits.canEscapeRight) return b to ((gridWidth - b.x + 1).toFloat() to 0f)
            if (limits.canEscapeTop) return b to (0f to (-b.height - b.y - 1).toFloat())
            if (limits.canEscapeBottom) return b to (0f to (gridHeight - b.y + 1).toFloat())
        }

        // 2. Look for any block that can move to unblock an exit path
        for (b in blocks.filter { !it.isEscaped }) {
            val limits = calculateLimits(b)
            if (b.direction.canMoveHorizontal) {
                if (limits.maxXOnBoard > b.x) return b to ((limits.maxXOnBoard - b.x).toFloat() to 0f)
                if (limits.minXOnBoard < b.x) return b to ((limits.minXOnBoard - b.x).toFloat() to 0f)
            }
            if (b.direction.canMoveVertical) {
                if (limits.maxYOnBoard > b.y) return b to (0f to (limits.maxYOnBoard - b.y).toFloat())
                if (limits.minYOnBoard < b.y) return b to (0f to (limits.minYOnBoard - b.y).toFloat())
            }
        }

        return null
    }
}
