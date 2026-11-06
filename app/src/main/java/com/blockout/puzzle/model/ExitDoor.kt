package com.blockout.puzzle.model

data class ExitDoor(
    val id: String,
    val side: ExitSide,
    val index: Int, // Grid index along the side: column for TOP/BOTTOM (0..width-1), row for LEFT/RIGHT (0..height-1)
    val color: BlockColor,
    val widthCells: Int = 1 // Support doors that span 1 or more cells
)

