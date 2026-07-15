package me.haroldmartin.einkui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EinkComponentsTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun buttonHasMinimumTargetAndDisabledState() {
        composeRule.setContent {
            EinkTheme {
                EinkButton(onClick = {}, enabled = false) { Text("Disabled") }
            }
        }

        composeRule.onNodeWithText("Disabled")
            .assertIsNotEnabled()
            .assertHeightIsAtLeast(48.dp)
    }

    @Test
    fun switchRowChangesImmediately() {
        composeRule.setContent {
            val checked = remember { mutableStateOf(false) }
            EinkTheme {
                EinkSwitchRow(checked = checked.value, onCheckedChange = { checked.value = it }) {
                    Text("Calendar")
                }
            }
        }

        composeRule.onNodeWithText("Calendar").performClick().assertIsOn()
    }

    @Test
    fun stepperHonorsBounds() {
        val value = mutableIntStateOf(0)
        composeRule.setContent {
            EinkTheme {
                EinkStepper(value = value.intValue, onValueChange = { value.intValue = it }, valueRange = 0..1)
            }
        }

        composeRule.onNodeWithContentDescription("Decrease").assertIsNotEnabled()
        composeRule.onNodeWithContentDescription("Increase").performClick()
        composeRule.runOnIdle { assertEquals(1, value.intValue) }
        composeRule.onNodeWithContentDescription("Increase").assertIsNotEnabled()
    }

    @Test
    fun colorChooserExposesNamedSelectedChoices() {
        composeRule.setContent {
            val selection = remember { mutableStateOf<EinkColorChoice>(EinkColorChoice.Random) }
            EinkTheme {
                EinkColorChooser(
                    selection = selection.value,
                    onSelectionChange = { selection.value = it },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Random").assertIsSelected()
        composeRule.onNodeWithContentDescription("Red").performClick().assertIsSelected()
    }

    @Test
    fun forcedPaneModesRenderExpectedSlots() {
        composeRule.setContent {
            EinkTheme {
                Box(modifier = Modifier.size(900.dp, 600.dp)) {
                    EinkAdaptivePaneLayout(
                        layoutMode = EinkLayoutMode.TwoPane,
                        primaryPane = { Text("Primary") },
                        secondaryPane = { Text("Secondary") },
                    )
                }
            }
        }

        composeRule.onNodeWithText("Primary").assertIsDisplayed()
        composeRule.onNodeWithText("Secondary").assertIsDisplayed()
    }

    @Test
    fun adaptivePaneRetainsStateWhenPaneLambdasChange() {
        val primaryVersion = mutableIntStateOf(0)
        val secondaryVersion = mutableIntStateOf(0)
        composeRule.setContent {
            EinkTheme {
                val currentPrimaryVersion = primaryVersion.intValue
                val currentSecondaryVersion = secondaryVersion.intValue
                EinkAdaptivePaneLayout(
                    layoutMode = EinkLayoutMode.TwoPane,
                    primaryPane = {
                        val clicks = remember { mutableIntStateOf(0) }
                        EinkButton(onClick = { clicks.intValue++ }) {
                            Text("Primary ${clicks.intValue} v$currentPrimaryVersion")
                        }
                    },
                    secondaryPane = {
                        val clicks = remember { mutableIntStateOf(0) }
                        EinkButton(onClick = { clicks.intValue++ }) {
                            Text("Secondary ${clicks.intValue} v$currentSecondaryVersion")
                        }
                    },
                )
            }
        }

        composeRule.onNodeWithText("Primary 0 v0").performClick()
        composeRule.onNodeWithText("Secondary 0 v0").performClick()
        composeRule.runOnIdle {
            primaryVersion.intValue++
            secondaryVersion.intValue++
        }

        composeRule.onNodeWithText("Primary 1 v1").assertIsDisplayed()
        composeRule.onNodeWithText("Secondary 1 v1").assertIsDisplayed()
    }

    @Test
    fun pickerDialogKeepsActionsOnScreen() {
        composeRule.setContent {
            EinkTheme {
                EinkPickerDialog(
                    onDismissRequest = {},
                    title = { Text("Choose item") },
                    primaryPane = { Box(modifier = Modifier.heightIn(max = 360.dp)) },
                    confirmButton = { EinkButton(onClick = {}) { Text("Done") } },
                    dismissButton = { EinkButton(onClick = {}) { Text("Cancel") } },
                )
            }
        }

        composeRule.onNodeWithText("Done").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").assertIsDisplayed()
    }
}
