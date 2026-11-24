package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// SoundStage Dhaka — Jamini Roy Patachitra & Rickshaw Paint Palette
val StageCharcoal = Color(0xFF111015)       // Deep Kohl Ink background
val StageSurface = Color(0xFF1B1821)        // Patachitra Charcoal Slate
val StageSurfaceElevated = Color(0xFF25212D)
val StageSurfaceHighest = Color(0xFF302B3A)
val StageOutline = Color(0xFF423A4D)        // Bold Jamini ink-warmed border
val StageOutlineSubtle = Color(0xFF2D2736)

// Turmeric / Marigold Gold (Primary CTA & Jamini Haldi Ochre)
val StageAmber = Color(0xFFF59E0B)
val StageAmberBright = Color(0xFFFBBF24)
val StageAmberDeep = Color(0xFFD97706)
val StageAmberContainer = Color(0xFF3D2706)
val StageOnAmber = Color(0xFF140D02)

// Patachitra Handmade Paper Typography
val StageOffWhite = Color(0xFFFAF5EC)
val StageTextSecondary = Color(0xFFC4BBB0)
val StageTextMuted = Color(0xFF8E857B)

// Jamini Roy & Dhaka Rickshaw Paint Accents
val JaminiTerracotta = Color(0xFFE04F2B)    // Kalighat Terracotta Vermilion
val JaminiTerracottaContainer = Color(0xFF3D170E)
val RickshawMagenta = Color(0xFFEC4899)     // Alta & Rickshaw Floral Pink
val RickshawTeal = Color(0xFF06B6D4)        // Peacock Teal
val RickshawCobalt = Color(0xFF2563EB)      // Jamini Krishna Cobalt
val StageEmerald = Color(0xFF10B981)        // Patachitra Leaf Green
val StageEmeraldContainer = Color(0xFF0B2E24)
val StageRose = Color(0xFFF43F5E)           // Alta Crimson
val StageRoseContainer = Color(0xFF3A0F1A)
val StageSky = Color(0xFF38BDF8)

// Instagram Brand Gradient
val InstagramYellow = Color(0xFFF58529)
val InstagramPink = Color(0xFFDD2A7B)
val InstagramPurple = Color(0xFF8134AF)
val InstagramBlue = Color(0xFF515BD4)

val InstagramGradientBrush = Brush.linearGradient(
    colors = listOf(InstagramYellow, InstagramPink, InstagramPurple, InstagramBlue)
)

val RickshawPaintBrush = Brush.horizontalGradient(
    colors = listOf(
        StageAmber,
        JaminiTerracotta,
        RickshawMagenta,
        RickshawTeal,
        StageEmerald
    )
)

val JaminiTerracottaBrush = Brush.horizontalGradient(
    colors = listOf(
        JaminiTerracotta,
        StageAmber
    )
)

val AmberSpotlightBrush = Brush.verticalGradient(
    colors = listOf(
        StageAmber.copy(alpha = 0.22f),
        StageCharcoal.copy(alpha = 0.0f)
    )
)
