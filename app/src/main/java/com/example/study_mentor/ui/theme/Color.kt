package com.example.study_mentor.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// Soothing Soft Orange Theme (Material 3)
// ==========================================

// Light Color Scheme Tokens
val SoftOrangePrimaryLight = Color(0xFFE06835)       // Gentle, soothing soft orange / warm terracotta
val SoftOrangeOnPrimaryLight = Color(0xFFFFFFFF)
val SoftOrangePrimaryContainerLight = Color(0xFFFFDBCE) // Soft pastel peach container
val SoftOrangeOnPrimaryContainerLight = Color(0xFF380C00)
val SoftOrangeInversePrimaryLight = Color(0xFFFFB596)

val SoftOrangeSecondaryLight = Color(0xFF86533C)     // Soothing warm sand / apricot accent
val SoftOrangeOnSecondaryLight = Color(0xFFFFFFFF)
val SoftOrangeSecondaryContainerLight = Color(0xFFFFDBCE)
val SoftOrangeOnSecondaryContainerLight = Color(0xFF331203)

val SoftOrangeTertiaryLight = Color(0xFF7A5832)      // Warm amber / honey balance
val SoftOrangeOnTertiaryLight = Color(0xFFFFFFFF)
val SoftOrangeTertiaryContainerLight = Color(0xFFFFDCBE)
val SoftOrangeOnTertiaryContainerLight = Color(0xFF2C1600)

val SoftOrangeBackgroundLight = Color(0xFFFFF8F6)    // Warm pearl / soft cream (eye-friendly)
val SoftOrangeOnBackgroundLight = Color(0xFF221A16)   // Soft dark charcoal brown
val SoftOrangeSurfaceLight = Color(0xFFFFFFFF)
val SoftOrangeOnSurfaceLight = Color(0xFF221A16)
val SoftOrangeSurfaceVariantLight = Color(0xFFF5DED6)
val SoftOrangeOnSurfaceVariantLight = Color(0xFF53433D)
val SoftOrangeSurfaceTintLight = Color(0xFFE06835)
val SoftOrangeInverseSurfaceLight = Color(0xFF382E2A)
val SoftOrangeInverseOnSurfaceLight = Color(0xFFFFEDE7)

val SoftOrangeErrorLight = Color(0xFFBA1A1A)
val SoftOrangeOnErrorLight = Color(0xFFFFFFFF)
val SoftOrangeErrorContainerLight = Color(0xFFFFDAD6)
val SoftOrangeOnErrorContainerLight = Color(0xFF410002)

val SoftOrangeOutlineLight = Color(0xFF85736C)
val SoftOrangeOutlineVariantLight = Color(0xFFD8C2BA)
val SoftOrangeScrimLight = Color(0xFF000000)

// Dark Color Scheme Tokens
val SoftOrangePrimaryDark = Color(0xFFFFB596)        // Gentle pastel soft orange glow
val SoftOrangeOnPrimaryDark = Color(0xFF551E00)
val SoftOrangePrimaryContainerDark = Color(0xFF773212)
val SoftOrangeOnPrimaryContainerDark = Color(0xFFFFDBCE)
val SoftOrangeInversePrimaryDark = Color(0xFFE06835)

val SoftOrangeSecondaryDark = Color(0xFFE7BCAB)
val SoftOrangeOnSecondaryDark = Color(0xFF432A1B)
val SoftOrangeSecondaryContainerDark = Color(0xFF5C3C2D)
val SoftOrangeOnSecondaryContainerDark = Color(0xFFFFDBCE)

val SoftOrangeTertiaryDark = Color(0xFFECC094)
val SoftOrangeOnTertiaryDark = Color(0xFF442B09)
val SoftOrangeTertiaryContainerDark = Color(0xFF5F411D)
val SoftOrangeOnTertiaryContainerDark = Color(0xFFFFDCBE)

val SoftOrangeBackgroundDark = Color(0xFF1B120E)     // Deep soothing espresso
val SoftOrangeOnBackgroundDark = Color(0xFFEDE0DB)
val SoftOrangeSurfaceDark = Color(0xFF221915)
val SoftOrangeOnSurfaceDark = Color(0xFFEDE0DB)
val SoftOrangeSurfaceVariantDark = Color(0xFF53433D)
val SoftOrangeOnSurfaceVariantDark = Color(0xFFD8C2BA)
val SoftOrangeSurfaceTintDark = Color(0xFFFFB596)
val SoftOrangeInverseSurfaceDark = Color(0xFFEDE0DB)
val SoftOrangeInverseOnSurfaceDark = Color(0xFF382E2A)

val SoftOrangeErrorDark = Color(0xFFFFB4AB)
val SoftOrangeOnErrorDark = Color(0xFF690005)
val SoftOrangeErrorContainerDark = Color(0xFF93000A)
val SoftOrangeOnErrorContainerDark = Color(0xFFFFDAD6)

val SoftOrangeOutlineDark = Color(0xFFA08C85)
val SoftOrangeOutlineVariantDark = Color(0xFF53433D)
val SoftOrangeScrimDark = Color(0xFF000000)

// Backward Compatibility Aliases & Direct Accent Colors
val PrimaryOrange = SoftOrangePrimaryLight
val PrimaryOrangeDark = SoftOrangePrimaryDark

// Compatibility alias so all existing screens immediately adopt the soothing soft orange:
val PrimaryBlue = PrimaryOrange

val SecondaryGreen = Color(0xFF10B981)
val BackgroundOffWhite = SoftOrangeBackgroundLight
val SurfaceWhite = Color(0xFFFFFFFF)
val TextDark = SoftOrangeOnBackgroundLight
val TextGray = SoftOrangeOutlineLight

// Dynamic RGB LED Spectrum (Magenta, Blue, Cyan, Green, Yellow, Red)
val RgbLedColors = listOf(
    Color(0xFFFF00FF), // Magenta
    Color(0xFF2979FF), // Electric Blue
    Color(0xFF00E5FF), // Cyan
    Color(0xFF00E676), // Green
    Color(0xFFFFEA00), // Yellow
    Color(0xFFFF1744), // Red
    Color(0xFFFF00FF)  // Back to Magenta for unbroken, perfectly seamless 360-degree rotation loop
)

val RgbChromaColors = RgbLedColors
