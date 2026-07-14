package me.haroldmartin.einkui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp

@Composable
@Suppress("LongParameterList")
fun EinkSurface(
    modifier: Modifier = Modifier,
    shape: Shape = EinkTheme.shapes.surface,
    backgroundColor: Color = EinkTheme.colors.surface,
    borderColor: Color = EinkTheme.colors.outline,
    borderWidth: Dp = EinkTheme.borders.standard,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    modifier = modifier
        .clip(shape)
        .background(backgroundColor)
        .border(borderWidth, borderColor, shape),
    content = content,
)

@Composable
fun EinkCard(
    modifier: Modifier = Modifier,
    borderWidth: Dp = EinkTheme.borders.standard,
    content: @Composable BoxScope.() -> Unit,
) = EinkSurface(
    modifier = modifier,
    borderWidth = borderWidth,
    content = content,
)
