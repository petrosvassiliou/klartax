package de.klartax.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Material 3 Expressive Farbpalette in:
 * - Dunkelrot (Primary: #7A1624 / #8B1E2D)
 * - Reinweiß & Kühles Alabaster-Weiß (Surface: #FFFFFF / Background: #F6F8FC)
 * - Königsblau (Secondary / FinTech-Akzent für Erstattungen & Kennzahlen: #1E3A8A / #1D4ED8)
 */
val KlartaxCrimsonPrimary = Color(0xFF7A1624)
val KlartaxCrimsonDark = Color(0xFF580E19)
val KlartaxCrimsonContainer = Color(0xFFFCE8EB)
val KlartaxOnCrimsonContainer = Color(0xFF3B060E)

val KlartaxRoyalBlue = Color(0xFF1E3A8A)
val KlartaxRoyalBlueBright = Color(0xFF1D4ED8)
val KlartaxRoyalBlueContainer = Color(0xFFDBEAFE)
val KlartaxOnRoyalBlueContainer = Color(0xFF0F2557)

val KlartaxPureWhite = Color(0xFFFFFFFF)
val KlartaxIceSurface = Color(0xFFF6F8FC)
val KlartaxCardBorder = Color(0xFFE2E8F0)

private val KlartaxLightColorScheme = lightColorScheme(
    primary = KlartaxCrimsonPrimary,
    onPrimary = KlartaxPureWhite,
    primaryContainer = KlartaxCrimsonContainer,
    onPrimaryContainer = KlartaxOnCrimsonContainer,
    secondary = KlartaxRoyalBlue,
    onSecondary = KlartaxPureWhite,
    secondaryContainer = KlartaxRoyalBlueContainer,
    onSecondaryContainer = KlartaxOnRoyalBlueContainer,
    tertiary = KlartaxRoyalBlueBright,
    onTertiary = KlartaxPureWhite,
    tertiaryContainer = Color(0xFFE0E7FF),
    onTertiaryContainer = Color(0xFF1E1B4B),
    background = KlartaxIceSurface,
    onBackground = Color(0xFF0F172A),
    surface = KlartaxPureWhite,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFEEF2F8),
    onSurfaceVariant = Color(0xFF334155),
    outline = Color(0xFFCBD5E1),
    outlineVariant = KlartaxCardBorder,
    error = Color(0xFFB91C1C),
    onError = KlartaxPureWhite
)

private val KlartaxDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB3BA),
    onPrimary = Color(0xFF5F0F1C),
    primaryContainer = Color(0xFF7A1624),
    onPrimaryContainer = Color(0xFFFFD9DC),
    secondary = Color(0xFF93C5FD),
    onSecondary = Color(0xFF0F2557),
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFFDBEAFE),
    tertiary = Color(0xFFBFDBFE),
    onTertiary = Color(0xFF172554),
    tertiaryContainer = Color(0xFF1D4ED8),
    onTertiaryContainer = Color(0xFFEFF6FF),
    background = Color(0xFF0B1120),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF1E293B),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569),
    error = Color(0xFFF87171),
    onError = Color(0xFF450A0A)
)

val KlartaxExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(26.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

private val KlartaxTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 19.sp,
        lineHeight = 25.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 15.5.sp,
        lineHeight = 21.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.5.sp,
        lineHeight = 21.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.5.sp,
        lineHeight = 16.sp
    )
)

@Composable
fun KlartaxTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) KlartaxDarkColorScheme else KlartaxLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = KlartaxExpressiveShapes,
        typography = KlartaxTypography,
        content = content
    )
}
