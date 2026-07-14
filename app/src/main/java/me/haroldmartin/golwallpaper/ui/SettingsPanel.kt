package me.haroldmartin.golwallpaper.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import me.haroldmartin.einkui.EinkExpandableSection
import me.haroldmartin.einkui.EinkOptionGroup
import me.haroldmartin.einkui.EinkSwitchRow
import me.haroldmartin.einkui.EinkTheme
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.GolSettings
import me.haroldmartin.golwallpaper.domain.OutputOrientation
import me.haroldmartin.golwallpaper.domain.RulePreset
import me.haroldmartin.golwallpaper.domain.WallpaperTarget

@Suppress("MagicNumber")
private val CELL_SIZES = listOf(5, 10, 20, 40)

@Suppress("MagicNumber")
private val UPDATE_INTERVALS_MINS = listOf(15L, 30L, 60L, 180L, 720L, 1440L)

@Suppress("MagicNumber")
private val BATTERY_THRESHOLDS = listOf(0, 10, 20, 30, 50)
private const val MINS_PER_HOUR = 60L

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

    EinkExpandableSection(
        expanded = areSettingsVisible,
        onExpandedChange = { areSettingsVisible = it },
        modifier = modifier,
        title = {
            Text(
                text = stringResource(R.string.settings),
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                style = EinkTheme.typography.title,
            )
        },
    ) {
        SettingsOptions(
            backgroundColor = backgroundColor,
            settings = settings,
            showWallpaperTarget = showWallpaperTarget,
            onBackgroundColorChange = onBackgroundColorChange,
            onSettingsChange = onSettingsChange,
        )
    }
}

@Composable
private fun SettingsOptions(
    backgroundColor: Int,
    settings: GolSettings,
    showWallpaperTarget: Boolean,
    onBackgroundColorChange: (Int) -> Unit,
    onSettingsChange: (GolSettings) -> Unit,
) = Column(
    modifier = Modifier.padding(start = EinkTheme.spacing.medium),
    verticalArrangement = Arrangement.spacedBy(EinkTheme.spacing.small),
) {
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
    OrientationOption(settings = settings, onSettingsChange = onSettingsChange)
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
        label = stringResource(R.string.wrap_edges_label),
        checked = settings.areEdgesWrapped,
        onCheckedChange = { wrap -> onSettingsChange(settings.copy(areEdgesWrapped = wrap)) },
    )
    SwitchRow(
        label = stringResource(R.string.show_stats_label),
        checked = settings.isStatsVisible,
        onCheckedChange = { show -> onSettingsChange(settings.copy(isStatsVisible = show)) },
    )
}

@Composable
private fun OrientationOption(
    settings: GolSettings,
    onSettingsChange: (GolSettings) -> Unit,
) {
    OptionRow(
        label = stringResource(R.string.orientation_label),
        options = listOf(
            stringResource(R.string.orientation_auto) to OutputOrientation.AUTO,
            stringResource(R.string.orientation_portrait) to OutputOrientation.PORTRAIT,
            stringResource(R.string.orientation_landscape) to OutputOrientation.LANDSCAPE,
        ),
        selected = settings.outputOrientation,
        onSelect = { orientation ->
            onSettingsChange(settings.copy(outputOrientation = orientation))
        },
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    EinkSwitchRow(checked = checked, onCheckedChange = onCheckedChange) {
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
    label: String?,
    options: List<Pair<String, T>>,
    selected: T,
    onSelect: (T) -> Unit,
) {
    val selectedOption = options.firstOrNull { (_, value) -> value == selected } ?: return
    EinkOptionGroup(
        options = options,
        selected = selectedOption,
        onSelectionChange = { (_, value) -> onSelect(value) },
        label = label,
        optionLabel = { option -> option.first },
    )
}
