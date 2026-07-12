package me.haroldmartin.golwallpaper

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.CalendarSource
import me.haroldmartin.golwallpaper.ui.CalendarSection
import me.haroldmartin.golwallpaper.ui.CalendarSectionCallbacks
import me.haroldmartin.golwallpaper.ui.theme.GoLWallpaperTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@RunWith(AndroidJUnit4::class)
class CalendarSectionTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun pickerShowsCalendarIdentityAndForwardsSelection() {
        val toggledId = AtomicReference<Long?>()
        val isConfirmed = AtomicBoolean(false)
        val isDismissed = AtomicBoolean(false)
        composeRule.setContent {
            GoLWallpaperTheme {
                CalendarSection(
                    settings = CalendarOverlaySettings(),
                    uiState = CalendarUiState(
                        sources = listOf(
                            CalendarSource(
                                id = 7,
                                displayName = "Work",
                                accountName = "person@example.com",
                                isPrimary = true,
                            ),
                        ),
                        draftSelectedIds = setOf(7),
                        isPickerVisible = true,
                    ),
                    callbacks = CalendarSectionCallbacks(
                        onPermissionResult = {},
                        onOpenPicker = {},
                        onDisable = {},
                        onSettingsChange = {},
                        onToggleDraft = toggledId::set,
                        onConfirmPicker = { isConfirmed.set(true) },
                        onDismissPicker = { isDismissed.set(true) },
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Work").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("person@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed().performClick()

        assertEquals(7L, toggledId.get())
        assertTrue(isConfirmed.get())
        assertTrue(isDismissed.get())
    }

    @Test
    fun enabledOverlayShowsSelectedCalendars() {
        composeRule.setContent {
            GoLWallpaperTheme {
                CalendarSection(
                    settings = CalendarOverlaySettings(
                        isEnabled = true,
                        selectedCalendarIds = setOf(7, 8),
                    ),
                    uiState = CalendarUiState(
                        sources = listOf(
                            CalendarSource(
                                id = 7,
                                displayName = "Work",
                                accountName = "person@example.com",
                                isPrimary = true,
                            ),
                            CalendarSource(
                                id = 8,
                                displayName = "Personal",
                                accountName = "person@example.com",
                                isPrimary = false,
                            ),
                        ),
                    ),
                    callbacks = CalendarSectionCallbacks(
                        onPermissionResult = {},
                        onOpenPicker = {},
                        onDisable = {},
                        onSettingsChange = {},
                        onToggleDraft = {},
                        onConfirmPicker = {},
                        onDismissPicker = {},
                    ),
                )
            }
        }

        composeRule.onNodeWithText("Calendar overlay").performClick()

        composeRule.onAllNodesWithText("Selected calendars:").assertCountEquals(0)
        composeRule.onNodeWithText("Work, Personal").assertIsDisplayed().assertHasClickAction()
        composeRule.onAllNodesWithText("Choose calendars").assertCountEquals(0)
    }
}
