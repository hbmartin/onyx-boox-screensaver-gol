package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.GolSettings
import me.haroldmartin.golwallpaper.domain.RulePreset
import me.haroldmartin.golwallpaper.domain.WallpaperTarget
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM
import me.haroldmartin.golwallpaper.ui.theme.SMALL

@Suppress("MagicNumber")
private val CELL_SIZES = listOf(5, 10, 20, 40)

@Suppress("MagicNumber")
private val UPDATE_INTERVALS_MINS = listOf(15L, 30L, 60L, 180L, 720L, 1440L)

@Suppress("MagicNumber")
private val BATTERY_THRESHOLDS = listOf(0, 10, 20, 30, 50)
private const val MINS_PER_HOUR = 60L
private val SELECTED_BORDER = 2.dp
private val UNSELECTED_BORDER = 1.dp

@Composable
@Suppress("LongParameterList")
fun SettingsPanel(
    backgroundColor: Int,
    settings: GolSettings,
    showWallpaperTarget: Boolean,
    onBackgroundColorChange: (Int) -> Unit,
    onSettingsChange: (GolSettings) -> Unit,
    modifier: Modifier = Modifier,
) {
    var areSettingsVisible by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(MEDIUM),
    ) {
        Row(
            modifier = Modifier.clickable { areSettingsVisible = !areSettingsVisible },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Disclosure(areSettingsVisible)
            Text(
                modifier = Modifier.padding(horizontal = MEDIUM),
                fontWeight = FontWeight.Bold,
                text = stringResource(R.string.settings),
            )
        }
        if (areSettingsVisible) {
            SettingsOptions(
                backgroundColor = backgroundColor,
                settings = settings,
                showWallpaperTarget = showWallpaperTarget,
                onBackgroundColorChange = onBackgroundColorChange,
                onSettingsChange = onSettingsChange,
            )
        }
    }
}

@Composable
private fun SettingsOptions(
    backgroundColor: Int,
    settings: GolSettings,
    showWallpaperTarget: Boolean,
    onBackgroundColorChange: (Int) -> Unit,
    onSettingsChange: (GolSettings) -> Unit,
) = Column(verticalArrangement = Arrangement.spacedBy(MEDIUM)) {
    ColorPicker(
        label = stringResource(R.string.bg_color),
        selectedColor = backgroundColor,
        onClick = onBackgroundColorChange,
    )
    OptionRow(
        label = stringResource(R.string.cell_size_label),
        options = CELL_SIZES.map { size ->
            stringResource(R.string.pixels_format, size) to size
        },
        selected = settings.cellSize,
        onSelect = { size -> onSettingsChange(settings.copy(cellSize = size)) },
    )
    OptionRow(
        label = stringResource(R.string.update_interval_label),
        options = UPDATE_INTERVALS_MINS.map { mins ->
            intervalLabel(mins) to mins
        },
        selected = settings.updateIntervalMins,
        onSelect = { mins -> onSettingsChange(settings.copy(updateIntervalMins = mins)) },
    )
    OptionRow(
        label = stringResource(R.string.battery_threshold_label),
        options = BATTERY_THRESHOLDS.map { pct ->
            batteryThresholdLabel(pct) to pct
        },
        selected = settings.batteryThresholdPct,
        onSelect = { pct -> onSettingsChange(settings.copy(batteryThresholdPct = pct)) },
    )
    if (showWallpaperTarget) {
        OptionRow(
            label = stringResource(R.string.wallpaper_target_label),
            options = listOf(
                stringResource(R.string.target_lock) to WallpaperTarget.LOCK,
                stringResource(R.string.target_home) to WallpaperTarget.HOME,
                stringResource(R.string.target_both) to WallpaperTarget.BOTH,
            ),
            selected = settings.wallpaperTarget,
            onSelect = { target -> onSettingsChange(settings.copy(wallpaperTarget = target)) },
        )
    }
    SwitchRow(
        label = stringResource(R.string.show_stats_label),
        checked = settings.isStatsVisible,
        onCheckedChange = { show -> onSettingsChange(settings.copy(isStatsVisible = show)) },
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.toggleable(
            value = checked,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MEDIUM),
    ) {
        Switch(
            checked = checked,
            onCheckedChange = null,
        )
        Text(label)
    }
}

@Composable
internal fun RulePreset.displayName(): String = stringResource(
    when (this) {
        RulePreset.CONWAY -> R.string.rule_conway
        RulePreset.HIGH_LIFE -> R.string.rule_high_life
        RulePreset.DAY_AND_NIGHT -> R.string.rule_day_and_night
        RulePreset.SEEDS -> R.string.rule_seeds
        RulePreset.LIFE_WITHOUT_DEATH -> R.string.rule_life_without_death
    },
)

@Composable
private fun batteryThresholdLabel(pct: Int): String = if (pct <= 0) {
    stringResource(R.string.battery_threshold_off)
} else {
    stringResource(R.string.percent_format, pct)
}

@Composable
private fun intervalLabel(mins: Long): String = if (mins < MINS_PER_HOUR) {
    stringResource(R.string.minutes_format, mins)
} else {
    stringResource(R.string.hours_format, mins / MINS_PER_HOUR)
}

@Composable
internal fun <T> OptionRow(
    label: String,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(SMALL)) {
        Text(text = label, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(SMALL),
        ) {
            options.forEach { (name, value) ->
                OptionButton(
                    name = name,
                    isSelected = value == selected,
                    onClick = { onSelect(value) },
                )
            }
        }
    }
}

@Composable
private fun OptionButton(name: String, isSelected: Boolean, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Button(
        modifier = Modifier
            .border(
                width = if (isSelected) SELECTED_BORDER else UNSELECTED_BORDER,
                color = colorScheme.outline,
            )
            .semantics { selected = isSelected },
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = colorScheme.surface,
            contentColor = colorScheme.onSurface,
        ),
    ) {
        Text(
            text = name,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
