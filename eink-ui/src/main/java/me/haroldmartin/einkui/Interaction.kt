package me.haroldmartin.einkui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role

@Composable
internal fun Modifier.einkClickable(
    enabled: Boolean = true,
    role: Role? = null,
    onClickLabel: String? = null,
    onClick: () -> Unit,
): Modifier = clickable(
    interactionSource = remember { MutableInteractionSource() },
    indication = null,
    enabled = enabled,
    role = role,
    onClickLabel = onClickLabel,
    onClick = onClick,
)
