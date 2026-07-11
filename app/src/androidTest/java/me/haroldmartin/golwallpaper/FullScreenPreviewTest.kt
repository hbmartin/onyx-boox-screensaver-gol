package me.haroldmartin.golwallpaper

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.click
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import me.haroldmartin.golwallpaper.ui.FullScreenPreviewContent
import me.haroldmartin.golwallpaper.ui.PREVIEW_BUTTON_TAG
import me.haroldmartin.golwallpaper.ui.PREVIEW_CONTROLS_TAG
import me.haroldmartin.golwallpaper.ui.PREVIEW_CONTROLS_TIMEOUT_MILLIS
import me.haroldmartin.golwallpaper.ui.PREVIEW_DIALOG_TAG
import me.haroldmartin.golwallpaper.ui.PreviewFloatingActionButton
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FullScreenPreviewTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun previewFloatingButtonForwardsClick() {
        val wasClicked = AtomicBoolean(false)
        composeRule.setContent {
            GoLWallpaperTheme {
                PreviewFloatingActionButton(onClick = { wasClicked.set(true) })
            }
        }

        composeRule.onNodeWithTag(PREVIEW_BUTTON_TAG).assertIsDisplayed().performClick()

        assertTrue(wasClicked.get())
    }

    @Test
    fun controlsAutoHideAndReturnWhenPreviewIsTapped() {
        composeRule.mainClock.autoAdvance = false
        composeRule.setContent {
            GoLWallpaperTheme {
                FullScreenPreviewContent(
                    state = readyState(),
                    onDismiss = {},
                    onPlayPause = {},
                    onStep = {},
                    onResync = {},
                    onFramePresent = {},
                )
            }
        }

        composeRule.onNodeWithTag(PREVIEW_CONTROLS_TAG).assertIsDisplayed()
        composeRule.mainClock.advanceTimeBy(PREVIEW_CONTROLS_TIMEOUT_MILLIS + 1)
        composeRule.waitForIdle()
        composeRule.onAllNodesWithTag(PREVIEW_CONTROLS_TAG).assertCountEquals(0)

        composeRule.onNodeWithTag(PREVIEW_DIALOG_TAG).performTouchInput { click() }
        composeRule.onNodeWithTag(PREVIEW_CONTROLS_TAG).assertIsDisplayed()
    }

    @Test
    fun controlsForwardActionsAndAcknowledgeFrame() {
        val state = mutableStateOf(readyState())
        val wasClosed = AtomicBoolean(false)
        val wasStepped = AtomicBoolean(false)
        val wasResynced = AtomicBoolean(false)
        val presentedFrame = AtomicLong(-1)
        composeRule.setContent {
            GoLWallpaperTheme {
                FullScreenPreviewContent(
                    state = state.value,
                    onDismiss = { wasClosed.set(true) },
                    onPlayPause = { state.value = state.value.copy(isPlaying = true) },
                    onStep = { wasStepped.set(true) },
                    onResync = { wasResynced.set(true) },
                    onFramePresent = presentedFrame::set,
                )
            }
        }

        composeRule.onNodeWithText("Step").performClick()
        composeRule.onNodeWithText("Play").performClick()
        composeRule.onNodeWithText("Pause").assertIsDisplayed()
        composeRule.onNodeWithText("Re-sync").performClick()
        composeRule.onNodeWithText("Close").performClick()

        assertTrue(wasClosed.get())
        assertTrue(wasStepped.get())
        assertTrue(wasResynced.get())
        assertEquals(FRAME_ID, presentedFrame.get())
    }

    private fun readyState() = PreviewUiState(
        isOpen = true,
        frame = PreviewFrame(
            id = FRAME_ID,
            image = ImageBitmap(width = 2, height = 2),
            bufferIndex = 0,
        ),
    )

    private companion object {
        const val FRAME_ID = 7L
    }
}
