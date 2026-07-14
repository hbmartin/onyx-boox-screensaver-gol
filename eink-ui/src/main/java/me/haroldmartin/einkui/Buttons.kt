@file:Suppress("MatchingDeclarationName")

package me.haroldmartin.einkui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

enum class EinkButtonEmphasis {
    Standard,
    Strong,
}

@Composable
@Suppress("LongParameterList")
fun EinkButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    role: Role = Role.Button,
    emphasis: EinkButtonEmphasis = EinkButtonEmphasis.Standard,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = EinkTheme.spacing.medium,
        vertical = EinkTheme.spacing.small,
    ),
    content: @Composable RowScope.() -> Unit,
) {
    var isFocused by remember { mutableStateOf(false) }
    val colors = EinkTheme.colors
    val shape = EinkTheme.shapes.control
    val borderWidth = when {
        isFocused -> EinkTheme.borders.strong
        emphasis == EinkButtonEmphasis.Strong -> EinkTheme.borders.strong
        else -> EinkTheme.borders.standard
    }
    val borderColor = when {
        !enabled -> colors.disabled
        isFocused -> colors.accent
        else -> colors.outline
    }
    val contentColor = if (enabled) colors.content else colors.disabled
    val textStyle = if (emphasis == EinkButtonEmphasis.Strong) {
        EinkTheme.typography.label.copy(fontWeight = FontWeight.Bold)
    } else {
        EinkTheme.typography.label
    }

    Row(
        modifier = modifier
            .defaultMinSize(
                minWidth = EinkTheme.layout.minimumTouchTarget,
                minHeight = EinkTheme.layout.minimumTouchTarget,
            )
            .onFocusChanged { state -> isFocused = state.isFocused }
            .clip(shape)
            .background(colors.surface)
            .border(borderWidth, borderColor, shape)
            .einkClickable(enabled = enabled, role = role, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(value = textStyle, content = { content() })
        }
    }
}

@Composable
fun EinkIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) = EinkButton(
    onClick = onClick,
    modifier = modifier.sizeIn(
        minWidth = EinkTheme.layout.minimumTouchTarget,
        minHeight = EinkTheme.layout.minimumTouchTarget,
    ),
    enabled = enabled,
    contentPadding = PaddingValues(EinkTheme.spacing.small),
    content = content,
)

@Composable
fun EinkFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) = EinkButton(
    onClick = onClick,
    modifier = modifier,
    emphasis = EinkButtonEmphasis.Strong,
    content = content,
)

@Composable
fun EinkLink(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = if (enabled) EinkTheme.colors.content else EinkTheme.colors.disabled,
) {
    Row(
        modifier = modifier
            .sizeIn(minHeight = EinkTheme.layout.minimumTouchTarget)
            .einkClickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            color = color,
            style = EinkTheme.typography.body,
            textDecoration = TextDecoration.Underline,
        )
    }
}
