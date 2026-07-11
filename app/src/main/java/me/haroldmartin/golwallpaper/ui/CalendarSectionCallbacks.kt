package me.haroldmartin.golwallpaper.ui

import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings

data class CalendarSectionCallbacks(
    val onPermissionResult: (Boolean) -> Unit,
    val onOpenPicker: () -> Unit,
    val onDisable: () -> Unit,
    val onSettingsChange: (CalendarOverlaySettings) -> Unit,
    val onToggleDraft: (Long) -> Unit,
    val onConfirmPicker: () -> Unit,
    val onDismissPicker: () -> Unit,
)
