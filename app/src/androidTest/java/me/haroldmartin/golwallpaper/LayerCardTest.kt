package me.haroldmartin.golwallpaper

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.haroldmartin.golwallpaper.domain.Layer
import me.haroldmartin.golwallpaper.domain.Patterns
import me.haroldmartin.golwallpaper.domain.RulePreset
import me.haroldmartin.golwallpaper.ui.LayerCallbacks
import me.haroldmartin.golwallpaper.ui.LayerCard
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LayerCardTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun expandedLayerShowsSelectionsAndCollapsibleRuleOptions() {
        composeRule.setContent {
            GoLWallpaperTheme {
                LayerCard(
                    layer = Layer(
                        rule = RulePreset.HIGH_LIFE.rule,
                        startingPattern = Patterns.ARK1.name,
                    ),
                    index = 0,
                    layerCount = 2,
                    callbacks = LayerCallbacks(
                        onEnabledChange = {},
                        onMoveUp = {},
                        onMoveDown = {},
                        onDelete = {},
                        onColorChange = {},
                        onRuleChange = {},
                        onResetPattern = { _, _ -> },
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Layer 1").performClick()

        composeRule.onNodeWithText("Game Rule: HighLife").assertIsDisplayed()
        composeRule.onNodeWithText("Reset Starting Pattern: ARK1").assertIsDisplayed()
        composeRule.onNodeWithText("Conway's Life").assertDoesNotExist()

        composeRule.onNodeWithText("Game Rule: HighLife").performClick()
        composeRule.onNodeWithText("Conway's Life").assertIsDisplayed()
    }
}
