package me.haroldmartin.golwallpaper.ui

data class LayersCallbacks(
    val onAdd: () -> Unit,
    val onRemove: (Int) -> Unit,
    val onMoveUp: (Int) -> Unit,
    val onMoveDown: (Int) -> Unit,
    val onEnabledChange: (Int, Boolean) -> Unit,
    val onColorChange: (Int, Int) -> Unit,
    val onRuleChange: (Int, String) -> Unit,
    val onResetPattern: (Int, String?, String?) -> Unit,
)
