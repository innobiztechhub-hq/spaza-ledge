package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ==========================================
// BESPOKE SIGNATURE PALETTE (COMBO 04)
// Ocean Blue (#2872A1) & Cloudy Sky (#CBDDE9)
// ==========================================

// Core Identity Colors from user specification
val OceanBlue = Color(0xFF2872A1)            // Signature Ocean Blue #2872A1
val CloudySky = Color(0xFFCBDDE9)            // Signature Cloudy Sky #CBDDE9

// Derived Tonal Harmony
val OceanBlueDeep = Color(0xFF164463)        // Deepest contrast ocean navy
val OceanBlueDark = Color(0xFF1E567B)        // Rich dark ocean blue
val OceanBlueMedium = Color(0xFF2872A1)      // Primary #2872A1
val OceanBlueLight = Color(0xFF4A90BC)       // Vibrant mid ocean
val CloudySkyBright = Color(0xFFB5D0E3)      // Vivid sky tone
val CloudySkyLight = Color(0xFFDDEAF3)       // Soft cloudy sky container
val CloudySkySoft = Color(0xFFEAF2F7)        // Light pill container
val CloudySkySubtle = Color(0xFFF3F8FB)      // Ultra-soft surface tint

// Core Background & Surfaces
val ImmersiveBackground = Color(0xFFF4F7FA)      // Clean canvas with subtle cloudy undertone
val ImmersiveSurface = Color(0xFFFFFFFF)         // Pure crisp white
val ImmersiveSurfaceElevated = Color(0xFFEDF4F9) // Card & panel background
val ImmersiveSurfaceCard = Color(0xFFFFFFFF)     // Card background
val ImmersiveBorder = Color(0xFFD5E3ED)          // Crisp border echoing Cloudy Sky
val ImmersiveBorderMedium = Color(0xFFCBDDE9)    // The authentic Cloudy Sky border
val ImmersiveBorderSolid = Color(0xFF88AEC7)     // Solid ocean border

// Primary & Containers
val ImmersivePrimary = OceanBlue                 // #2872A1
val ImmersivePrimaryDark = OceanBlueDark         // #1E567B
val ImmersivePrimaryContainer = CloudySkySoft    // #EAF2F7
val ImmersiveOnPrimaryContainer = OceanBlueDark  // #1E567B

// Secondary & Containers
val ImmersiveSecondary = OceanBlueLight          // #4A90BC
val ImmersiveSecondaryContainer = CloudySky      // #CBDDE9
val ImmersiveOnSecondaryContainer = OceanBlueDeep// #164463

val ImmersiveTertiary = OceanBlue                // Unify with signature palette
val ImmersiveTertiaryContainer = CloudySkySoft   // Soft cloudy container

// Typography Palette (High-contrast, crisp and legible in sunlit shops)
val ImmersiveTextPrimary = Color(0xFF0F2637)     // Rich deep ocean slate for impeccable readability
val ImmersiveTextSecondary = Color(0xFF35566C)   // Readable slate-ocean subtitle
val ImmersiveTextMuted = Color(0xFF65869B)       // Ocean-slate hint text

// Status & Warnings (As requested: warnings/red labeling preserved)
val AccentRose = Color(0xFFDC2626)               // Explicit warning / critical red
val AccentRoseLight = Color(0xFFFEE2E2)          // Light red alert container
val AccentRoseBorder = Color(0xFFFCA5A5)
val AccentEmerald = OceanBlue                    // Metric positive is harmonized to Ocean Blue!
val AccentEmeraldLight = CloudySkyLight
val AccentAmber = Color(0xFFD97706)

// Backward Compatible Tokens mapped to the unified palette
val BrandNavy900 = OceanBlueDeep
val BrandNavy800 = OceanBlueDark
val BrandNavy700 = Color(0xFF244A63)
val BrandNavy600 = Color(0xFF355E7B)

val BrandBlue = OceanBlue
val BrandBlueLight = OceanBlueLight
val BrandIndigo = OceanBlueDeep
val BrandViolet = OceanBlueDark
val BrandCyan = OceanBlueLight
val BrandSky = CloudySkyBright

val SurfaceDark = Color(0xFF0F2637)
val SurfaceDarkElevated = Color(0xFF16374D)
val SurfaceBorderDark = Color(0xFF285472)
val BackgroundDark = Color(0xFF0A1C28)

val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceLightElevated = Color(0xFFEDF4F9)
val SurfaceBorderLight = ImmersiveBorder
val BackgroundLight = ImmersiveBackground

val TextPrimaryDark = Color(0xFFF4F8FA)
val TextSecondaryDark = Color(0xFFCBDDE9)
val TextMutedDark = Color(0xFF8BA9BC)

val TextPrimaryLight = ImmersiveTextPrimary
val TextSecondaryLight = ImmersiveTextSecondary
val TextMutedLight = ImmersiveTextMuted

// ==========================================
// UNIQUE SIGNATURE GRADIENTS
// Blending Ocean Blue (#2872A1) & Cloudy Sky (#CBDDE9)
// ==========================================

// Primary horizontal bold gradient for main action buttons
val OceanGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF1B5377),
        Color(0xFF2872A1),
        Color(0xFF458CBA)
    )
)

// Linear diagonal blend from deep Ocean Blue to Cloudy Sky
val OceanSkyGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E567B),
        Color(0xFF2872A1),
        Color(0xFF7FAFCF),
        Color(0xFFCBDDE9)
    )
)

// Subtle card gradient with clean white into faint cloudy sky
val CloudyCardGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFF4F8FB)
    )
)

// Hero header banner gradient
val OceanHeroGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF164463),
        Color(0xFF2872A1)
    )
)

// Pill container gradient
val SkyPillGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFE2EEF5),
        Color(0xFFCBDDE9)
    )
)

// Secondary button gradient
val SkyButtonGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFF1F6FA),
        Color(0xFFDCEAF3)
    )
)

val ImmersiveHeroGradient = OceanHeroGradient
val ImmersiveCardGradient = CloudyCardGradient
val ImmersiveAccentGradient = OceanGradient
val HeroGradientBrush = OceanGradient
val StatCardGradient = CloudyCardGradient
val CyanIndigoGradient = OceanGradient
val EmeraldGradient = OceanGradient
val AmberGradient = OceanSkyGradient
val RoseGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFFDC2626),
        Color(0xFFEF4444)
    )
)
