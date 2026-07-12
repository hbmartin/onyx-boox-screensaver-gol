package me.haroldmartin.golwallpaper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GoLWallpaperTheme {
                MainScreen()
            }
        }
    }
}
