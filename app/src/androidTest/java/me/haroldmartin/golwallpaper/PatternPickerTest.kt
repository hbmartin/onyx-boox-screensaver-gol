package me.haroldmartin.golwallpaper

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.haroldmartin.golwallpaper.ui.PatternPicker
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PatternPickerTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun opensDialogInsideVerticallyScrollableParent() {
        composeRule.setContent {
            GoLWallpaperTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    PatternPicker(selectedPattern = null, onClick = { _, _ -> })
                }
            }
        }

        composeRule.onNodeWithContentDescription("Triangle").assertIsDisplayed()
        composeRule.onNodeWithText("Reset Starting Pattern: Random Noise").performClick()

        composeRule.onNodeWithText("Reset Starting Pattern").assertIsDisplayed()
        composeRule.onNodeWithText("Random Noise").assertIsDisplayed()
    }
}
