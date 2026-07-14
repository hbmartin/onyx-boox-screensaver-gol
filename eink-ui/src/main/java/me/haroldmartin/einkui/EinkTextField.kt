package me.haroldmartin.einkui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation

@Composable
@Suppress("LongParameterList")
fun EinkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: String? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    var isFocused by remember { mutableStateOf(false) }
    val colors = EinkTheme.colors
    val borderColor = when {
        !enabled -> colors.disabled
        isFocused -> colors.accent
        else -> colors.outline
    }
    val borderWidth = if (isFocused || isError) EinkTheme.borders.strong else EinkTheme.borders.standard
    val contentColor = if (enabled) colors.content else colors.disabled
    val semanticsModifier = if (isError && supportingText != null) {
        Modifier.semantics { error(supportingText) }
    } else {
        Modifier
    }

    Column(modifier = modifier.then(semanticsModifier)) {
        if (label != null) {
            Text(
                text = label,
                color = contentColor,
                style = EinkTheme.typography.label,
                modifier = Modifier.padding(bottom = EinkTheme.spacing.extraSmall),
            )
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .sizeIn(minHeight = EinkTheme.layout.minimumTouchTarget)
                .onFocusChanged { state -> isFocused = state.isFocused }
                .clip(EinkTheme.shapes.control)
                .border(borderWidth, borderColor, EinkTheme.shapes.control)
                .padding(EinkTheme.spacing.small),
            enabled = enabled,
            textStyle = EinkTheme.typography.body.copy(color = contentColor),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            visualTransformation = visualTransformation,
            cursorBrush = SolidColor(contentColor),
        )
        if (supportingText != null) {
            Text(
                text = supportingText,
                color = contentColor,
                style = EinkTheme.typography.supporting,
                modifier = Modifier.padding(top = EinkTheme.spacing.extraSmall),
            )
        }
    }
}
