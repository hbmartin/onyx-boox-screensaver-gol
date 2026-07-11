package me.haroldmartin.golwallpaper.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import me.haroldmartin.golwallpaper.CalendarUiIssue
import me.haroldmartin.golwallpaper.CalendarUiState
import me.haroldmartin.golwallpaper.R
import me.haroldmartin.golwallpaper.domain.CalendarHorizon
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.CalendarSource
import me.haroldmartin.golwallpaper.domain.OverlayCorner
import me.haroldmartin.golwallpaper.domain.OverlaySize
import me.haroldmartin.golwallpaper.ui.theme.Disclosure
import me.haroldmartin.golwallpaper.ui.theme.MEDIUM

private val CALENDAR_LIST_MAX_HEIGHT = 360.dp

@Composable
@Suppress("LongParameterList")
fun CalendarSection(
    settings: CalendarOverlaySettings,
    uiState: CalendarUiState,
    onPermissionResult: (Boolean) -> Unit,
    onOpenPicker: () -> Unit,
    onDisable: () -> Unit,
    onSettingsChange: (CalendarOverlaySettings) -> Unit,
    onToggleDraft: (Long) -> Unit,
    onConfirmPicker: () -> Unit,
    onDismissPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = onPermissionResult,
    )
    val requestCalendarAccess = {
        if (
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CALENDAR,
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            onOpenPicker()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_CALENDAR)
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MEDIUM)) {
        Row(
            modifier = Modifier.clickable { isExpanded = !isExpanded },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Disclosure(isExpanded)
            Text(
                modifier = Modifier.padding(horizontal = MEDIUM),
                fontWeight = FontWeight.Bold,
                text = stringResource(R.string.calendar_section_title),
            )
        }
        if (isExpanded) {
            CalendarOptions(
                settings = settings,
                issue = uiState.issue,
                onEnabledChange = { enabled ->
                    if (!enabled) {
                        onDisable()
                    } else {
                        requestCalendarAccess()
                    }
                },
                onOpenPicker = requestCalendarAccess,
                onSettingsChange = onSettingsChange,
            )
        }
    }

    if (uiState.isPickerVisible) {
        CalendarPickerDialog(
            sources = uiState.sources,
            selectedIds = uiState.draftSelectedIds,
            onToggle = onToggleDraft,
            onConfirm = onConfirmPicker,
            onDismiss = onDismissPicker,
        )
    }
}

@Composable
private fun CalendarOptions(
    settings: CalendarOverlaySettings,
    issue: CalendarUiIssue?,
    onEnabledChange: (Boolean) -> Unit,
    onOpenPicker: () -> Unit,
    onSettingsChange: (CalendarOverlaySettings) -> Unit,
) = Column(verticalArrangement = Arrangement.spacedBy(MEDIUM)) {
    Row(
        modifier = Modifier.toggleable(
            value = settings.isEnabled,
            role = Role.Switch,
            onValueChange = onEnabledChange,
        ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MEDIUM),
    ) {
        Switch(checked = settings.isEnabled, onCheckedChange = null)
        Text(stringResource(R.string.calendar_enable))
    }
    if (issue != null) Text(text = issue.displayText())
    if (settings.isEnabled) {
        Button(onClick = onOpenPicker) {
            Text(stringResource(R.string.calendar_choose))
        }
        OptionRow(
            label = stringResource(R.string.calendar_horizon),
            options = listOf(
                stringResource(R.string.calendar_horizon_today) to CalendarHorizon.TODAY,
                stringResource(R.string.calendar_horizon_24_hours) to CalendarHorizon.NEXT_24_HOURS,
                stringResource(R.string.calendar_horizon_7_days) to CalendarHorizon.NEXT_7_DAYS,
            ),
            selected = settings.horizon,
            onSelect = { horizon -> onSettingsChange(settings.copy(horizon = horizon)) },
        )
        OptionRow(
            label = stringResource(R.string.calendar_position),
            options = listOf(
                stringResource(R.string.calendar_top_left) to OverlayCorner.TOP_LEFT,
                stringResource(R.string.calendar_top_right) to OverlayCorner.TOP_RIGHT,
                stringResource(R.string.calendar_bottom_left) to OverlayCorner.BOTTOM_LEFT,
                stringResource(R.string.calendar_bottom_right) to OverlayCorner.BOTTOM_RIGHT,
            ),
            selected = settings.corner,
            onSelect = { corner -> onSettingsChange(settings.copy(corner = corner)) },
        )
        OptionRow(
            label = stringResource(R.string.calendar_size),
            options = listOf(
                stringResource(R.string.calendar_small) to OverlaySize.SMALL,
                stringResource(R.string.calendar_medium) to OverlaySize.MEDIUM,
                stringResource(R.string.calendar_large) to OverlaySize.LARGE,
            ),
            selected = settings.size,
            onSelect = { size -> onSettingsChange(settings.copy(size = size)) },
        )
    }
}

@Composable
private fun CalendarPickerDialog(
    sources: List<CalendarSource>,
    selectedIds: Set<Long>,
    onToggle: (Long) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calendar_choose)) },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = CALENDAR_LIST_MAX_HEIGHT)) {
                items(items = sources, key = CalendarSource::id) { source ->
                    CalendarSourceRow(
                        source = source,
                        isSelected = source.id in selectedIds,
                        onToggle = { onToggle(source.id) },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.calendar_done)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.calendar_cancel)) }
        },
    )
}

@Composable
private fun CalendarSourceRow(
    source: CalendarSource,
    isSelected: Boolean,
    onToggle: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = isSelected, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(vertical = MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MEDIUM),
    ) {
        Checkbox(checked = isSelected, onCheckedChange = null)
        Column {
            Text(source.displayName, fontWeight = FontWeight.Bold)
            Text(source.accountName)
            if (source.isPrimary) Text(stringResource(R.string.calendar_primary))
        }
    }
}

@Composable
private fun CalendarUiIssue.displayText(): String = stringResource(
    when (this) {
        CalendarUiIssue.PERMISSION_REQUIRED -> R.string.calendar_permission_required
        CalendarUiIssue.NO_CALENDARS -> R.string.calendar_no_sources
        CalendarUiIssue.SOURCES_UNAVAILABLE -> R.string.calendar_sources_unavailable
    },
)
