package me.haroldmartin.einkui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp

@Composable
@Suppress("MagicNumber")
fun EinkDisclosure(
    expanded: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Dp = EinkTheme.spacing.medium,
    color: Color = EinkTheme.colors.content,
    contentDescription: String? = null,
) {
    val semanticsModifier = if (contentDescription == null) {
        modifier
    } else {
        modifier.semantics { this.contentDescription = contentDescription }
    }
    Canvas(modifier = semanticsModifier.size(iconSize)) {
        val path = Path().apply {
            if (expanded) {
                moveTo(size.width * 0.15f, size.height * 0.3f)
                lineTo(size.width * 0.85f, size.height * 0.3f)
                lineTo(size.width * 0.5f, size.height * 0.75f)
            } else {
                moveTo(size.width * 0.3f, size.height * 0.15f)
                lineTo(size.width * 0.75f, size.height * 0.5f)
                lineTo(size.width * 0.3f, size.height * 0.85f)
            }
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}

@Composable
fun EinkDisclosureRow(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .einkClickable(role = Role.Button) { onExpandedChange(!expanded) }
            .padding(vertical = EinkTheme.spacing.small),
        horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EinkDisclosure(expanded = expanded)
        content()
    }
}

@Composable
fun EinkExpandableSection(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(EinkTheme.borders.strong, EinkTheme.colors.outline)
            .padding(EinkTheme.spacing.medium),
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
    ) {
        EinkDisclosureRow(
            expanded = expanded,
            onExpandedChange = onExpandedChange,
            content = title,
        )
        if (expanded) {
            Column(
                modifier = Modifier.padding(start = EinkTheme.spacing.medium),
                verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
                content = content,
            )
        }
    }
}

@Composable
fun EinkExpandableSection(
    title: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
    initiallyExpanded: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    var isExpanded by remember { mutableStateOf(initiallyExpanded) }
    EinkExpandableSection(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = modifier,
        title = title,
        content = content,
    )
}
