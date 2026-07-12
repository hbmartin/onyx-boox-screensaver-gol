package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.CUSTOM_STARTING_PATTERN
import me.haroldmartin.golwallpaper.domain.Patterns
import me.haroldmartin.golwallpaper.domain.isParseablePattern
import me.haroldmartin.golwallpaper.ui.theme.AppButton
import me.haroldmartin.golwallpaper.ui.theme.COLOR_SCHEME
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.LARGE
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM
import me.haroldmartin.golwallpaper.ui.theme.SMALL

private const val SCREEN_FRACTION = 0.6f
private val DIALOG_BORDER_WIDTH = 2.dp

@Composable
fun PatternPicker(
    selectedPattern: String?,
    onClick: (String?, String?) -> Unit,
) {
    var isDialogVisible by remember { mutableStateOf(false) }
    val selectedName = selectedPattern.displayName()

    Row(
        modifier = Modifier.clickable { isDialogVisible = true },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Disclosure(isOpen = false)
        Text(
            modifier = Modifier.padding(horizontal = MEDIUM),
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
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.border(
            width = DIALOG_BORDER_WIDTH,
            color = Color.Black,
            shape = RectangleShape,
        ),
        shape = RectangleShape,
        title = { Text(stringResource(R.string.reset_pattern)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(SMALL),
                modifier = Modifier
                    .heightIn(max = patternsMaxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(start = LARGE),
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
        confirmButton = {},
        dismissButton = {
            AppButton(onClick = onDismiss) {
                Text(stringResource(R.string.calendar_cancel))
            }
        },
    )
}

@Composable
private fun PatternButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    AppButton(onClick = onClick) {
        Text(
            modifier = Modifier.padding(horizontal = MEDIUM, vertical = 0.dp),
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

    OutlinedTextField(
        value = text,
        onValueChange = { value ->
            text = value
            isError = false
        },
        isError = isError,
        label = {
            Text(
                text = stringResource(R.string.paste_rle_label),
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            )
        },
        modifier = Modifier.fillMaxWidth(),
    )
    if (isError) {
        Text(
            text = stringResource(R.string.invalid_rle),
            color = COLOR_SCHEME.error,
        )
    }
    AppButton(
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
            modifier = Modifier.padding(horizontal = MEDIUM, vertical = 0.dp),
            fontWeight = FontWeight.Normal,
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
