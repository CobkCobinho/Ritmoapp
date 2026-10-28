package br.com.ritmo.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Ink = Color(0xFF26352F)
val Muted = Color(0xFF6E786F)
val Paper = Color(0xFFF7F7EF)
val Orange = Color(0xFFCE593C)
val Peach = Color(0xFFF5E8DC)
val Green = Color(0xFF47745A)
val Line = Color(0xFFE2E6DA)

@Composable
fun RitmoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Orange, onPrimary = Color.White,
            secondary = Green, onSecondary = Color.White,
            background = Paper, onBackground = Ink,
            surface = Color.White, onSurface = Ink,
            surfaceVariant = Peach, onSurfaceVariant = Ink,
            outline = Line, outlineVariant = Line
        ),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 38.sp),
            headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 30.sp),
            titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
            titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp),
            bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 21.sp),
            labelLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        ),
        content = content
    )
}
