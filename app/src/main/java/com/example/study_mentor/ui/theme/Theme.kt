package com.example.study_mentor.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = SoftOrangePrimaryLight,
    onPrimary = SoftOrangeOnPrimaryLight,
    primaryContainer = SoftOrangePrimaryContainerLight,
    onPrimaryContainer = SoftOrangeOnPrimaryContainerLight,
    inversePrimary = SoftOrangeInversePrimaryLight,
    secondary = SoftOrangeSecondaryLight,
    onSecondary = SoftOrangeOnSecondaryLight,
    secondaryContainer = SoftOrangeSecondaryContainerLight,
    onSecondaryContainer = SoftOrangeOnSecondaryContainerLight,
    tertiary = SoftOrangeTertiaryLight,
    onTertiary = SoftOrangeOnTertiaryLight,
    tertiaryContainer = SoftOrangeTertiaryContainerLight,
    onTertiaryContainer = SoftOrangeOnTertiaryContainerLight,
    background = SoftOrangeBackgroundLight,
    onBackground = SoftOrangeOnBackgroundLight,
    surface = SoftOrangeSurfaceLight,
    onSurface = SoftOrangeOnSurfaceLight,
    surfaceVariant = SoftOrangeSurfaceVariantLight,
    onSurfaceVariant = SoftOrangeOnSurfaceVariantLight,
    surfaceTint = SoftOrangeSurfaceTintLight,
    inverseSurface = SoftOrangeInverseSurfaceLight,
    inverseOnSurface = SoftOrangeInverseOnSurfaceLight,
    error = SoftOrangeErrorLight,
    onError = SoftOrangeOnErrorLight,
    errorContainer = SoftOrangeErrorContainerLight,
    onErrorContainer = SoftOrangeOnErrorContainerLight,
    outline = SoftOrangeOutlineLight,
    outlineVariant = SoftOrangeOutlineVariantLight,
    scrim = SoftOrangeScrimLight
)

private val DarkColorScheme = darkColorScheme(
    primary = SoftOrangePrimaryDark,
    onPrimary = SoftOrangeOnPrimaryDark,
    primaryContainer = SoftOrangePrimaryContainerDark,
    onPrimaryContainer = SoftOrangeOnPrimaryContainerDark,
    inversePrimary = SoftOrangeInversePrimaryDark,
    secondary = SoftOrangeSecondaryDark,
    onSecondary = SoftOrangeOnSecondaryDark,
    secondaryContainer = SoftOrangeSecondaryContainerDark,
    onSecondaryContainer = SoftOrangeOnSecondaryContainerDark,
    tertiary = SoftOrangeTertiaryDark,
    onTertiary = SoftOrangeOnTertiaryDark,
    tertiaryContainer = SoftOrangeTertiaryContainerDark,
    onTertiaryContainer = SoftOrangeOnTertiaryContainerDark,
    background = SoftOrangeBackgroundDark,
    onBackground = SoftOrangeOnBackgroundDark,
    surface = SoftOrangeSurfaceDark,
    onSurface = SoftOrangeOnSurfaceDark,
    surfaceVariant = SoftOrangeSurfaceVariantDark,
    onSurfaceVariant = SoftOrangeOnSurfaceVariantDark,
    surfaceTint = SoftOrangeSurfaceTintDark,
    inverseSurface = SoftOrangeInverseSurfaceDark,
    inverseOnSurface = SoftOrangeInverseOnSurfaceDark,
    error = SoftOrangeErrorDark,
    onError = SoftOrangeOnErrorDark,
    errorContainer = SoftOrangeErrorContainerDark,
    onErrorContainer = SoftOrangeOnErrorContainerDark,
    outline = SoftOrangeOutlineDark,
    outlineVariant = SoftOrangeOutlineVariantDark,
    scrim = SoftOrangeScrimDark
)

@Composable
fun Study_MentorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Use false by default to ensure custom brand colors are applied
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}