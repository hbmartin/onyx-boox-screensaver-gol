package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Gray
import androidx.compose.ui.graphics.Color.Companion.White

val COLOR_SCHEME = lightColorScheme(
    primary = White,
    onPrimary = Black,
    secondary = Black,
    tertiary = Gray,
    background = White,
)

@Composable
fun GoLWallpaperTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = COLOR_SCHEME,
        typography = TYPOGRAPHY,
        content = content,
    )
}
