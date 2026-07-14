@file:Suppress("MagicNumber", "UnusedPrivateMember")

package me.haroldmartin.einkui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Preview(showBackground = true, widthDp = 480)
@Composable
private fun MonochromeComponentsPreview() {
    EinkTheme {
        ComponentGallery()
    }
}

@Preview(showBackground = true, widthDp = 900)
@Composable
private fun AccentComponentsPreview() {
    EinkTheme(accent = Color(0xFF546E7A)) {
        ComponentGallery()
    }
}

@Composable
private fun ComponentGallery() {
    var isChecked by remember { mutableStateOf(true) }
    var radio by remember { mutableStateOf("One") }
    var stepper by remember { mutableIntStateOf(2) }
    var choice by remember { mutableStateOf<EinkColorChoice>(EinkColorChoice.Random) }
    Column(
        modifier = Modifier.padding(EinkTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
    ) {
        EinkButton(onClick = {}, emphasis = EinkButtonEmphasis.Strong) { Text("Strong action") }
        EinkButton(onClick = {}, enabled = false) { Text("Disabled action") }
        EinkSwitchRow(checked = isChecked, onCheckedChange = { isChecked = it }) { Text("Switch") }
        EinkCheckboxRow(checked = isChecked, onCheckedChange = { isChecked = it }) { Text("Checkbox") }
        EinkRadioGroup(
            options = listOf("One", "Two"),
            selected = radio,
            onSelectionChange = { radio = it },
        ) { option ->
            Text(option)
        }
        EinkStepper(
            value = stepper,
            onValueChange = { stepper = it },
            valueRange = 0..4,
            modifier = Modifier.fillMaxWidth(),
            label = "Stepper",
        )
        EinkColorChooser(selection = choice, onSelectionChange = { choice = it })
    }
}
