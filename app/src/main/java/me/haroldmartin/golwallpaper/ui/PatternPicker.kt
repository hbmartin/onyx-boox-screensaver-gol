package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.einkui.EinkButton
import me.haroldmartin.einkui.EinkChoiceButton
import me.haroldmartin.einkui.EinkDisclosureRow
import me.haroldmartin.einkui.EinkPickerDialog
import me.haroldmartin.einkui.EinkTextField
import me.haroldmartin.einkui.EinkTheme
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.CUSTOM_STARTING_PATTERN
import me.haroldmartin.golwallpaper.domain.Patterns
import me.haroldmartin.golwallpaper.domain.isParseablePattern

private const val SCREEN_FRACTION = 0.6f

@Composable
fun PatternPicker(
    selectedPattern: String?,
    onClick: (String?, String?) -> Unit,
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val selectedName = selectedPattern.displayName()

    EinkDisclosureRow(
        expanded = false,
        onExpandedChange = { isDialogVisible = true },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
            fontWeight = FontWeight.Bold,
            text = stringResource(R.string.reset_pattern_selected, selectedName),
        )
    }

    if (isDialogVisible) {
        PatternPickerDialog(
            selectedPattern = selectedPattern,
            onSelect = { pattern, patternName ->
                onClick(pattern, patternName)
                isDialogVisible = false
            },
            onDismiss = { isDialogVisible = false },
        )
    }
}

@Composable
private fun PatternPickerDialog(
    selectedPattern: String?,
    onSelect: (String?, String?) -> Unit,
    onDismiss: () -> Unit,
) {
    val patternsMaxHeight = LocalConfiguration.current.screenHeightDp.dp * SCREEN_FRACTION
    EinkPickerDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reset_pattern)) },
        primaryPane = {
            Column(
                verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraSmall),
                modifier = Modifier
                    .heightIn(max = patternsMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(start = EinkTheme.spacing.medium),
            ) {
                PatternButton(
                    text = stringResource(R.string.random_noise),
                    isSelected = selectedPattern == null,
                    onClick = { onSelect(null, null) },
                )
                Patterns.entries.forEach { pattern ->
                    PatternButton(
                        text = pattern.name,
                        isSelected = selectedPattern == pattern.name,
                        onClick = { onSelect(pattern.value, pattern.name) },
                    )
                }
                RleInput(
                    isSelected = selectedPattern == CUSTOM_STARTING_PATTERN,
                    onApply = { rle -> onSelect(rle, CUSTOM_STARTING_PATTERN) },
                )
            }
        },
        dismissButton = {
            EinkButton(onClick = onDismiss) {
                Text(stringResource(R.string.calendar_cancel))
            }
        },
    )
}

@Composable
private fun PatternButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    EinkChoiceButton(selected = isSelected, onClick = onClick) {
        Text(
            modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            text = text,
        )
    }
}

@Composable
@Suppress("MultipleEmitters")
private fun RleInput(isSelected: Boolean, onApply: (String) -> Unit) {
    var text by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    EinkTextField(
        value = text,
        onValueChange = { value ->
            text = value
            isError = false
        },
        isError = isError,
        label = stringResource(R.string.paste_rle_label) + if (isSelected) " ✓" else "",
        supportingText = if (isError) stringResource(R.string.invalid_rle) else null,
        modifier = Modifier.fillMaxWidth(),
    )
    EinkButton(
        onClick = {
            val rle = text.trim()
            if (isParseablePattern(rle)) {
                onApply(rle)
                text = ""
            } else {
                isError = true
            }
        },
    ) {
        Text(
            modifier = Modifier.padding(horizontal = EinkTheme.spacing.small),
            text = stringResource(R.string.apply_rle),
        )
    }
}

@Composable
private fun String?.displayName(): String = when (this) {
    null -> stringResource(R.string.random_noise)
    CUSTOM_STARTING_PATTERN -> stringResource(R.string.custom_rle)
    else -> this
}
