package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// One UI 9 Dark Glass Palette (Primary Reference Aesthetic)
val OneUiDarkBgStart = Color(0xFF0B0E23)
val OneUiDarkBgEnd = Color(0xFF17183B)
val OneUiGlassSurface = Color(0xFF1C2042)
val OneUiGlassCard = Color(0xFF232852)
val OneUiGlassBorder = Color(0x388B95C9)

// One UI 9 Accents
val OneUiPrimaryBlue = Color(0xFF3B82F6)
val OneUiElectricPurple = Color(0xFF7C3AED)
val OneUiVioletGlow = Color(0xFFA855F7)
val OneUiCyanAccent = Color(0xFF06B6D4)
val OneUiPinkAccent = Color(0xFFEC4899)
val OneUiEmeraldBattery = Color(0xFF10B981)
val OneUiAmberSun = Color(0xFFFBBF24)
val OneUiOrangeFolder = Color(0xFFF59E0B)

// AMOLED Pure Black Mode
val AmoledBlackBg = Color(0xFF000000)
val AmoledSurface = Color(0xFF0D0F18)
val AmoledCard = Color(0xFF141726)

// Light Mode Palette
val OneUiLightBg = Color(0xFFF2F4FA)
val OneUiLightSurface = Color(0xFFFFFFFF)
val OneUiLightCard = Color(0xFFE8ECF7)

// Text Colors
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF475569)

data class AccentPreset(
    val id: String,
    val nameAr: String,
    val primary: Color,
    val secondary: Color
)

val OneUiAccentPresets = listOf(
    AccentPreset("one_ui_purple_blue", "بنفسجي وأزرق One UI 9", Color(0xFF6366F1), Color(0xFFA855F7)),
    AccentPreset("galaxy_blue", "أزرق سامسونج الملكي", Color(0xFF2563EB), Color(0xFF38BDF8)),
    AccentPreset("emerald_neon", "زمردي متألق", Color(0xFF10B981), Color(0xFF34D399)),
    AccentPreset("rose_magenta", "وردي مجري", Color(0xFFEC4899), Color(0xFFF43F5E)),
    AccentPreset("amber_gold", "ذهبي فاخر", Color(0xFFF59E0B), Color(0xFFFBBF24)),
    AccentPreset("cyan_crystal", "سماوي كريستالي", Color(0xFF06B6D4), Color(0xFF22D3EE))
)

fun parseHexColor(hex: String, fallback: Color = OneUiPrimaryBlue): Color {
    return try {
        val clean = hex.removePrefix("#")
        val longVal = clean.toLong(16)
        if (clean.length == 6) {
            Color(longVal or 0xFF000000L)
        } else {
            Color(longVal)
        }
    } catch (_: Exception) {
        fallback
    }
}
