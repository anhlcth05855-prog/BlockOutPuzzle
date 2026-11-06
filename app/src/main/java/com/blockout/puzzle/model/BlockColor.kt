package com.blockout.puzzle.model

import androidx.compose.ui.graphics.Color

enum class BlockColor(
    val displayName: String,
    val mainColor: Color,
    val darkColor: Color,
    val lightColor: Color,
    val studColor: Color,
    val hexCode: String
) {
    BLUE(
        displayName = "Blue",
        mainColor = Color(0xFF00A2FF),
        darkColor = Color(0xFF006BB3),
        lightColor = Color(0xFF68C8FF),
        studColor = Color(0xFF38B6FF),
        hexCode = "#00A2FF"
    ),
    RED(
        displayName = "Red",
        mainColor = Color(0xFFFF334B),
        darkColor = Color(0xFFB8182B),
        lightColor = Color(0xFFFF7A8A),
        studColor = Color(0xFFFF5266),
        hexCode = "#FF334B"
    ),
    GREEN(
        displayName = "Green",
        mainColor = Color(0xFF00E676),
        darkColor = Color(0xFF00964F),
        lightColor = Color(0xFF66FFA6),
        studColor = Color(0xFF26EB8A),
        hexCode = "#00E676"
    ),
    YELLOW(
        displayName = "Yellow",
        mainColor = Color(0xFFFFB800),
        darkColor = Color(0xFFB38000),
        lightColor = Color(0xFFFFCF4D),
        studColor = Color(0xFFFFC21A),
        hexCode = "#FFB800"
    ),
    ORANGE(
        displayName = "Orange",
        mainColor = Color(0xFFFF7A00),
        darkColor = Color(0xFFB35500),
        lightColor = Color(0xFFFFA54D),
        studColor = Color(0xFFFF8F26),
        hexCode = "#FF7A00"
    ),
    PURPLE(
        displayName = "Purple",
        mainColor = Color(0xFFA855F7),
        darkColor = Color(0xFF7322BD),
        lightColor = Color(0xFFC48BFA),
        studColor = Color(0xFFB56EF8),
        hexCode = "#A855F7"
    ),
    WHITE(
        displayName = "White",
        mainColor = Color(0xFFE2E8F0),
        darkColor = Color(0xFF94A3B8),
        lightColor = Color(0xFFFFFFFF),
        studColor = Color(0xFFCBD5E1),
        hexCode = "#E2E8F0"
    ),
    BLACK(
        displayName = "Black",
        mainColor = Color(0xFF333642),
        darkColor = Color(0xFF1E2027),
        lightColor = Color(0xFF535768),
        studColor = Color(0xFF424656),
        hexCode = "#333642"
    ),
    PINK(
        displayName = "Pink",
        mainColor = Color(0xFFFF4081),
        darkColor = Color(0xFFC51162),
        lightColor = Color(0xFFFF80AB),
        studColor = Color(0xFFFF5C93),
        hexCode = "#FF4081"
    ),
    CYAN(
        displayName = "Cyan",
        mainColor = Color(0xFF00E5FF),
        darkColor = Color(0xFF0099AB),
        lightColor = Color(0xFF6EFFFF),
        studColor = Color(0xFF26EAFF),
        hexCode = "#00E5FF"
    );

    companion object {
        fun fromName(name: String): BlockColor {
            return entries.find { it.name.equals(name, ignoreCase = true) } ?: BLUE
        }
    }
}

