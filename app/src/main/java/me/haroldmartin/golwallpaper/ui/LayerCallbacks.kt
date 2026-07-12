package me.haroldmartin.golwallpaper.ui

data class LayerCallbacks(
    val onMoveUp: () -> Unit,
    val onMoveDown: () -> Unit,
    val onDelete: () -> Unit,
    val onColorChange: (Int) -> Unit,
    val onRuleChange: (String) -> Unit,
    val onResetPattern: (pattern: String?, startingPattern: String?) -> Unit,
)
