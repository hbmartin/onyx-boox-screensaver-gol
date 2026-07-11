package me.haroldmartin.golwallpaper.ui

data class LayerCallbacks(
    val onEnabledChange: (Boolean) -> Unit,
    val onMoveUp: () -> Unit,
    val onMoveDown: () -> Unit,
    val onDelete: () -> Unit,
    val onColorChange: (Int) -> Unit,
    val onRuleChange: (String) -> Unit,
    val onResetPattern: (String?) -> Unit,
)
