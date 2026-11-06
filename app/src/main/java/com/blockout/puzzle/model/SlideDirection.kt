package com.blockout.puzzle.model

enum class SlideDirection {
    HORIZONTAL,
    VERTICAL,
    ALL;

    val canMoveHorizontal: Boolean
        get() = this == HORIZONTAL || this == ALL

    val canMoveVertical: Boolean
        get() = this == VERTICAL || this == ALL
}

