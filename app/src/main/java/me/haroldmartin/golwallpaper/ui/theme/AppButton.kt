package me.haroldmartin.golwallpaper.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private const val DISABLED_ALPHA = 0.38f
private val BUTTON_BORDER_WIDTH = 1.dp

@Composable
internal fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderWidth: Dp = BUTTON_BORDER_WIDTH,
    content: @Composable RowScope.() -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val disabledColor = colorScheme.onSurface.copy(alpha = DISABLED_ALPHA)
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RectangleShape,
        border = BorderStroke(
            width = borderWidth,
            color = if (enabled) colorScheme.outline else disabledColor,
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
            disabledContainerColor = colorScheme.surface,
            disabledContentColor = disabledColor,
        ),
        content = content,
    )
}
