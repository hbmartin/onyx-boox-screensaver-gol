package me.haroldmartin.golwallpaper

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.CalendarSource
import me.haroldmartin.golwallpaper.ui.CalendarSection
import me.haroldmartin.golwallpaper.ui.CalendarSectionCallbacks
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
        composeRule.setContent {
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
                    onDismissPicker = {},
                ),
            )
        }

        composeRule.onNodeWithText("Work").assertIsDisplayed().performClick()
        composeRule.onNodeWithText("person@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("Done").performClick()

        assertEquals(7L, toggledId.get())
        assertTrue(isConfirmed.get())
    }
}
