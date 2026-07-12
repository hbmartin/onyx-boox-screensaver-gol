package me.haroldmartin.golwallpaper

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.haroldmartin.golwallpaper.domain.GolSettings
import me.haroldmartin.golwallpaper.domain.OutputOrientation
import me.haroldmartin.golwallpaper.ui.SettingsPanel
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsPanelTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun orientationOptionsUpdateSettings() {
        var updatedSettings: GolSettings? = null
        composeRule.setContent {
            GoLWallpaperTheme {
                SettingsPanel(
                    backgroundColor = Color.White.toArgb(),
                    settings = GolSettings(),
                    showWallpaperTarget = true,
                    onBackgroundColorChange = {},
                    onSettingsChange = { settings -> updatedSettings = settings },
                )
            }
        }

        composeRule.onNodeWithText("Settings").performClick()
        composeRule.onNodeWithText("Auto").assertIsDisplayed()
        composeRule.onNodeWithText("Portrait").assertIsDisplayed()
        composeRule.onNodeWithText("Landscape").assertIsDisplayed().performClick()

        composeRule.runOnIdle {
            assertEquals(OutputOrientation.LANDSCAPE, updatedSettings?.outputOrientation)
        }
    }
}
