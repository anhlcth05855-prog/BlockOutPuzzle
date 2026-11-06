package com.blockout.puzzle.data

import com.blockout.puzzle.model.Block
import com.blockout.puzzle.model.BlockColor
import com.blockout.puzzle.model.ExitDoor
import com.blockout.puzzle.model.ExitSide
import com.blockout.puzzle.model.Level
import com.blockout.puzzle.model.Obstacle
import com.blockout.puzzle.model.ObstacleType
import com.blockout.puzzle.model.SlideDirection
import java.util.Calendar
import kotlin.random.Random

object LevelRepository {

    private val baseLevels = listOf(
        // Level 1: Tutorial - First Escape (Identical to iOS video Level 1)
        Level(
            id = 1,
            name = "First Escape",
            description = "Slide the blocks directly out to matching doors!",
            difficulty = "EASY",
            world = 1,
            gridWidth = 5,
            gridHeight = 5,
            blocks = listOf(
                Block("b1", BlockColor.BLUE, x = 1, y = 0, width = 1, height = 3, direction = SlideDirection.VERTICAL),
                Block("b2", BlockColor.RED, x = 3, y = 2, width = 1, height = 3, direction = SlideDirection.VERTICAL)
            ),
            doors = listOf(
                ExitDoor("d1", ExitSide.TOP, index = 1, widthCells = 2, color = BlockColor.BLUE),
                ExitDoor("d2", ExitSide.BOTTOM, index = 2, widthCells = 2, color = BlockColor.RED)
            ),
            maxMoves = 15,
            parMoves = 2,
            timeSeconds = 300
        ),

        // Level 2: Two Pathways (Identical to iOS video Level 2)
        Level(
            id = 2,
            name = "Two Pathways",
            description = "Swipe blocks into their matching exit gates!",
            difficulty = "EASY",
            world = 1,
            gridWidth = 5,
            gridHeight = 5,
            blocks = listOf(
                Block("b1", BlockColor.GREEN, x = 1, y = 2, width = 1, height = 3, direction = SlideDirection.VERTICAL),
                Block("b2", BlockColor.ORANGE, x = 3, y = 1, width = 1, height = 3, direction = SlideDirection.VERTICAL)
            ),
            doors = listOf(
                ExitDoor("d1", ExitSide.TOP, index = 1, widthCells = 2, color = BlockColor.GREEN),
                ExitDoor("d2", ExitSide.BOTTOM, index = 2, widthCells = 2, color = BlockColor.ORANGE)
            ),
            maxMoves = 15,
            parMoves = 2,
            timeSeconds = 300
        ),

        // Level 3: Dual Gateways (Identical to iOS video Level 3)
        Level(
            id = 3,
            name = "Dual Gateways",
            description = "Coordinate moves to clear exits for all blocks.",
            difficulty = "NORMAL",
            world = 1,
            gridWidth = 5,
            gridHeight = 5,
            blocks = listOf(
                Block("b1", BlockColor.BLUE, x = 0, y = 0, width = 1, height = 2, direction = SlideDirection.VERTICAL),
                Block("b2", BlockColor.RED, x = 2, y = 2, width = 1, height = 3, direction = SlideDirection.VERTICAL),
                Block("b3", BlockColor.YELLOW, x = 3, y = 1, width = 1, height = 3, direction = SlideDirection.VERTICAL)
            ),
            doors = listOf(
                ExitDoor("d1", ExitSide.LEFT, index = 0, widthCells = 2, color = BlockColor.BLUE),
                ExitDoor("d2", ExitSide.TOP, index = 2, widthCells = 2, color = BlockColor.RED),
                ExitDoor("d3", ExitSide.RIGHT, index = 1, widthCells = 3, color = BlockColor.YELLOW),
                ExitDoor("d4", ExitSide.BOTTOM, index = 1, widthCells = 2, color = BlockColor.RED)
            ),
            maxMoves = 20,
            parMoves = 3,
            timeSeconds = 300
        ),

        // Level 4: Rock Obstacle Intro
        Level(
            id = 4,
            name = "Rock Outskirts",
            description = "Navigate around heavy rocks or destroy them using the Hammer booster!",
            difficulty = "NORMAL",
            world = 1,
            gridWidth = 5,
            gridHeight = 5,
            blocks = listOf(
                Block("b1", BlockColor.CYAN, x = 1, y = 1, width = 1, height = 2, direction = SlideDirection.VERTICAL, symbol = "diamond"),
                Block("b2", BlockColor.YELLOW, x = 2, y = 3, width = 2, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "star"),
                Block("b3", BlockColor.PINK, x = 3, y = 0, width = 1, height = 2, direction = SlideDirection.VERTICAL, symbol = "circle")
            ),
            doors = listOf(
                ExitDoor("d1", ExitSide.TOP, index = 1, color = BlockColor.CYAN),
                ExitDoor("d2", ExitSide.RIGHT, index = 3, color = BlockColor.YELLOW),
                ExitDoor("d3", ExitSide.BOTTOM, index = 3, color = BlockColor.PINK)
            ),
            obstacles = listOf(
                Obstacle("obs1", x = 2, y = 1, type = ObstacleType.ROCK)
            ),
            maxMoves = 24,
            parMoves = 9,
            timeSeconds = 180
        ),

        // Level 5: Grid Lock
        Level(
            id = 5,
            name = "Grid Lock",
            description = "Space is tight. Every move counts towards 3 stars.",
            difficulty = "NORMAL",
            world = 1,
            gridWidth = 6,
            gridHeight = 6,
            blocks = listOf(
                Block("b1", BlockColor.BLUE, x = 1, y = 0, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "diamond"),
                Block("b2", BlockColor.RED, x = 2, y = 1, width = 3, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "star"),
                Block("b3", BlockColor.GREEN, x = 0, y = 4, width = 3, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "square"),
                Block("b4", BlockColor.ORANGE, x = 4, y = 2, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "circle"),
                Block("b5", BlockColor.WHITE, x = 2, y = 3, width = 2, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "cross")
            ),
            doors = listOf(
                ExitDoor("d1", ExitSide.TOP, index = 1, color = BlockColor.BLUE),
                ExitDoor("d2", ExitSide.RIGHT, index = 1, color = BlockColor.RED),
                ExitDoor("d3", ExitSide.LEFT, index = 4, color = BlockColor.GREEN),
                ExitDoor("d4", ExitSide.BOTTOM, index = 4, color = BlockColor.ORANGE),
                ExitDoor("d5", ExitSide.RIGHT, index = 3, color = BlockColor.WHITE)
            ),
            maxMoves = 30,
            parMoves = 12,
            timeSeconds = 200
        ),

        // Level 36: The Canyon Mill (From the user's screenshot)
        Level(
            id = 36,
            name = "The Canyon Mill",
            description = "Rotating four-quadrant mill mechanisms driving canyon gates.",
            difficulty = "ADEPT",
            world = 1,
            gridWidth = 6,
            gridHeight = 6,
            blocks = listOf(
                Block("b_blue", BlockColor.BLUE, x = 1, y = 0, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "diamond"),
                Block("b_white", BlockColor.WHITE, x = 0, y = 3, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "cross"),
                Block("b_black", BlockColor.BLACK, x = 2, y = 3, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "star"),
                Block("b_purple", BlockColor.PURPLE, x = 3, y = 3, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "square"),
                Block("b_red", BlockColor.PINK, x = 5, y = 2, width = 1, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "circle"),
                Block("b_green", BlockColor.GREEN, x = 4, y = 4, width = 1, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "circle"),
                Block("b_orange", BlockColor.ORANGE, x = 5, y = 1, width = 1, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "star")
            ),
            doors = listOf(
                // Top doors
                ExitDoor("d_top_white", ExitSide.TOP, index = 0, color = BlockColor.WHITE),
                ExitDoor("d_top_blue", ExitSide.TOP, index = 1, color = BlockColor.BLUE),
                ExitDoor("d_top_black", ExitSide.TOP, index = 2, color = BlockColor.BLACK),
                ExitDoor("d_top_purple", ExitSide.TOP, index = 3, color = BlockColor.PURPLE),
                ExitDoor("d_top_green", ExitSide.TOP, index = 4, color = BlockColor.GREEN),
                // Left doors
                ExitDoor("d_left_pink", ExitSide.LEFT, index = 2, color = BlockColor.PINK),
                ExitDoor("d_left_green", ExitSide.LEFT, index = 4, color = BlockColor.GREEN),
                // Right doors
                ExitDoor("d_right_orange", ExitSide.RIGHT, index = 1, color = BlockColor.ORANGE)
            ),
            maxMoves = 34,
            parMoves = 18,
            timeSeconds = 161
        )
    )

    fun getAllLevels(): List<Level> {
        val list = mutableListOf<Level>()
        list.addAll(baseLevels.filter { it.id <= 5 })

        for (lvl in 6..35) {
            list.add(generateGuaranteedNonOverlappingLevel(lvl))
        }

        baseLevels.find { it.id == 36 }?.let { list.add(it) }

        return list.sortedBy { it.id }
    }

    fun getLevel(id: Int): Level {
        return getAllLevels().find { it.id == id } ?: getAllLevels().first()
    }

    fun getDailyPuzzle(): Level {
        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
        val year = calendar.get(Calendar.YEAR)
        val seed = (year * 366 + dayOfYear).toLong()

        val rnd = Random(seed)
        val colors = listOf(BlockColor.BLUE, BlockColor.RED, BlockColor.GREEN, BlockColor.YELLOW, BlockColor.PURPLE, BlockColor.ORANGE)
        val shuffledColors = colors.shuffled(rnd)

        val blocks = listOf(
            Block("daily_1", shuffledColors[0], x = 1, y = 1, width = 1, height = 3, direction = SlideDirection.VERTICAL, symbol = "star"),
            Block("daily_2", shuffledColors[1], x = 2, y = 2, width = 2, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "diamond"),
            Block("daily_3", shuffledColors[2], x = 4, y = 0, width = 1, height = 2, direction = SlideDirection.VERTICAL, symbol = "circle"),
            Block("daily_4", shuffledColors[3], x = 0, y = 4, width = 3, height = 1, direction = SlideDirection.HORIZONTAL, symbol = "square")
        )

        val doors = listOf(
            ExitDoor("d_daily_1", ExitSide.TOP, index = 1, color = shuffledColors[0]),
            ExitDoor("d_daily_2", ExitSide.RIGHT, index = 2, color = shuffledColors[1]),
            ExitDoor("d_daily_3", ExitSide.BOTTOM, index = 4, color = shuffledColors[2]),
            ExitDoor("d_daily_4", ExitSide.LEFT, index = 4, color = shuffledColors[3])
        )

        return Level(
            id = 9999,
            name = "Daily Challenge",
            description = "A fresh deterministic daily puzzle! Earn +100 coins & stars.",
            difficulty = "ADEPT",
            world = 1,
            gridWidth = 6,
            gridHeight = 6,
            blocks = blocks,
            doors = doors,
            maxMoves = 28,
            parMoves = 14,
            timeSeconds = 180
        )
    }

    /**
     * Mathematically guarantees 100% collision-free placement with no overlapping blocks.
     */
    private fun generateGuaranteedNonOverlappingLevel(id: Int): Level {
        val seed = id * 7919L
        val rnd = Random(seed)
        val gridW = 6
        val gridH = 6

        val diffName = when {
            id < 12 -> "NORMAL"
            id < 22 -> "ADEPT"
            id < 30 -> "HARD"
            else -> "SUPER HARD"
        }

        val availableColors = listOf(
            BlockColor.BLUE, BlockColor.RED, BlockColor.GREEN,
            BlockColor.YELLOW, BlockColor.PURPLE, BlockColor.ORANGE,
            BlockColor.WHITE, BlockColor.BLACK, BlockColor.PINK, BlockColor.CYAN
        ).shuffled(rnd)

        val gridOccupied = Array(gridH) { BooleanArray(gridW) }

        // Optional obstacle
        val obstacles = mutableListOf<Obstacle>()
        if (id > 10 && id % 3 == 0) {
            val ox = 2
            val oy = 2
            obstacles.add(Obstacle("obs_$id", x = ox, y = oy, type = ObstacleType.ROCK))
            gridOccupied[oy][ox] = true
        }

        val blocks = mutableListOf<Block>()
        val doors = mutableListOf<ExitDoor>()
        val symbols = listOf("star", "diamond", "square", "circle", "cross")

        val targetCount = (4 + (id % 4)).coerceIn(4, 7)

        // Try placing blocks with strictly empty cells
        for (i in 0 until targetCount) {
            val color = availableColors[i % availableColors.size]
            val isVertical = (i % 2 == 0)
            val len = if (rnd.nextBoolean()) 2 else 3

            val w = if (isVertical) 1 else len
            val h = if (isVertical) len else 1

            // Find all valid non-overlapping coordinates
            val validPositions = mutableListOf<Pair<Int, Int>>()
            for (r in 0..(gridH - h)) {
                for (c in 0..(gridW - w)) {
                    var fits = true
                    for (checkR in r until (r + h)) {
                        for (checkC in c until (c + w)) {
                            if (gridOccupied[checkR][checkC]) {
                                fits = false
                                break
                            }
                        }
                        if (!fits) break
                    }
                    if (fits) {
                        validPositions.add(c to r)
                    }
                }
            }

            if (validPositions.isNotEmpty()) {
                val (chosenX, chosenY) = validPositions[rnd.nextInt(validPositions.size)]

                // Mark grid as occupied
                for (checkR in chosenY until (chosenY + h)) {
                    for (checkC in chosenX until (chosenX + w)) {
                        gridOccupied[checkR][checkC] = true
                    }
                }

                blocks.add(
                    Block(
                        id = "b_${id}_$i",
                        color = color,
                        x = chosenX,
                        y = chosenY,
                        width = w,
                        height = h,
                        direction = if (isVertical) SlideDirection.VERTICAL else SlideDirection.HORIZONTAL,
                        symbol = symbols[i % symbols.size]
                    )
                )

                // Add matching exit door on the appropriate boundary
                val side = if (isVertical) {
                    if (chosenY < gridH / 2) ExitSide.TOP else ExitSide.BOTTOM
                } else {
                    if (chosenX < gridW / 2) ExitSide.LEFT else ExitSide.RIGHT
                }

                val doorIndex = if (side.isVertical) chosenX else chosenY
                // Avoid duplicate doors with conflicting colors at the exact same side & index
                doors.removeAll { it.side == side && it.index == doorIndex }
                doors.add(ExitDoor(id = "d_${id}_$i", side = side, index = doorIndex, color = color))
            }
        }

        return Level(
            id = id,
            name = "Level $id: ${getLevelTitle(id)}",
            description = "Navigate color gates, avoid block collisions and clear the board.",
            difficulty = diffName,
            world = 1,
            gridWidth = gridW,
            gridHeight = gridH,
            blocks = blocks,
            doors = doors,
            obstacles = obstacles,
            maxMoves = 25 + (id % 12),
            parMoves = 12 + (id % 6),
            timeSeconds = 150 + (id % 50)
        )
    }

    private fun getLevelTitle(id: Int): String {
        val titles = listOf(
            "Neon Outskirts", "Sapphire Crossing", "Emerald Maze", "Amber Switchback",
            "Obsidian Chamber", "Crimson Flow", "Prism Junction", "Lego Bastion",
            "Midnight Shuffle", "Velvet Gateway", "Starlight Labyrinth", "Cobalt Trench",
            "Iron Spire", "Crystal Tunnel", "Topaz Spiral", "Quartz Matrix"
        )
        return titles[(id - 1) % titles.size]
    }
}
