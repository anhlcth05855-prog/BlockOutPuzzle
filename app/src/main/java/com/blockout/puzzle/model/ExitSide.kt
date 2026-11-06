package com.blockout.puzzle.model

enum class ExitSide {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT;

    val isVertical: Boolean
        get() = this == TOP || this == BOTTOM

    val isHorizontal: Boolean
        get() = this == LEFT || this == RIGHT

    val opposite: ExitSide
        get() = when (this) {
            TOP -> BOTTOM
            BOTTOM -> TOP
            LEFT -> RIGHT
            RIGHT -> LEFT
        }
}

