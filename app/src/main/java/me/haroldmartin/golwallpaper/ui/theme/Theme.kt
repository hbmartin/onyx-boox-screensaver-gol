package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.toArgb

sealed class Colors(val displayName: String, val value: Color) {
    object Red : Colors("Red", Color(0xFFEF5350))
    object Pink : Colors("Pink", Color(0xFFBA68C8))
    object Purple : Colors("Purple", Color(0xFF673AB7))
    object Cyan : Colors("Cyan", Color(0xFF00ACC1))
    object Green : Colors("Green", Color(0xFF4CAF50))
    object Yellow : Colors("Yellow", Color(0xFFFFEB3B))
    object Blue : Colors("Blue", Color(0xFF546E7A))
    object Black : Colors("Black", Color(0xFF212121))
    object White : Colors("White", Color(0xFFFFFFFF))

    companion object {
        val list: List<Colors> = listOf(Red, Pink, Purple, Cyan, Green, Yellow, Blue, Black, White)
    }
}

val ColorScheme = lightColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Black,
    tertiary = Gray,
)


@Composable
fun GoLWallpaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ColorScheme,
        typography = Typography,
        content = content
    )
}