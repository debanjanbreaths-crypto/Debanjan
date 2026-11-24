package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val SoundStageDarkColorScheme = darkColorScheme(
    primary = StageAmber,
    onPrimary = StageOnAmber,
    primaryContainer = StageAmberContainer,
    onPrimaryContainer = StageAmberBright,
    secondary = JaminiTerracotta,
    onSecondary = StageOffWhite,
    secondaryContainer = JaminiTerracottaContainer,
    onSecondaryContainer = StageOffWhite,
    tertiary = RickshawTeal,
    onTertiary = StageCharcoal,
    tertiaryContainer = StageEmeraldContainer,
    onTertiaryContainer = StageEmerald,
    background = StageCharcoal,
    onBackground = StageOffWhite,
    surface = StageSurface,
    onSurface = StageOffWhite,
    surfaceVariant = StageSurfaceElevated,
    onSurfaceVariant = StageTextSecondary,
    surfaceContainerLowest = StageCharcoal,
    surfaceContainerLow = StageSurface,
    surfaceContainer = StageSurfaceElevated,
    surfaceContainerHigh = StageSurfaceHighest,
    surfaceContainerHighest = StageSurfaceHighest,
    outline = StageOutline,
    outlineVariant = StageOutlineSubtle,
    error = StageRose,
    onError = StageOffWhite,
    errorContainer = StageRoseContainer,
    onErrorContainer = StageRose
)

val SoundStageShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun SoundStageTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = SoundStageDarkColorScheme,
        typography = SoundStageTypography,
        shapes = SoundStageShapes,
        content = content
    )
}
