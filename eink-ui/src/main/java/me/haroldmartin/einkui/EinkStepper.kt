package me.haroldmartin.einkui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

@Composable
@Suppress("LongParameterList")
fun EinkStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    modifier: Modifier = Modifier,
    step: Int = 1,
    label: String? = null,
    valueFormatter: (Int) -> String = Int::toString,
) {
    require(step > 0) { "step must be positive" }
    require(!valueRange.isEmpty()) { "valueRange must not be empty" }
    val decrementDescription = stringResource(R.string.eink_decrease)
    val incrementDescription = stringResource(R.string.eink_increase)
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.extraSmall),
    ) {
        if (label != null) {
            Text(text = label, style = EinkTheme.typography.label, fontWeight = FontWeight.Bold)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            EinkIconButton(
                onClick = { onValueChange((value - step).coerceAtLeast(valueRange.first)) },
                enabled = value > valueRange.first,
                modifier = Modifier.semantics { contentDescription = decrementDescription },
            ) {
                Text("−")
            }
            Text(
                text = valueFormatter(value),
                modifier = Modifier.weight(1f),
                style = EinkTheme.typography.body,
            )
            EinkIconButton(
                onClick = { onValueChange((value + step).coerceAtMost(valueRange.last)) },
                enabled = value < valueRange.last,
                modifier = Modifier.semantics { contentDescription = incrementDescription },
            ) {
                Text("+")
            }
        }
    }
}
