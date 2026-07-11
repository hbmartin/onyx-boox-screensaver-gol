package me.haroldmartin.golwallpaper.domain

data class Layer(
    val fgColor: Int = DEFAULT_FG,
    val rule: String = DEFAULT_RULE,
    val state: String? = null,
    val generation: Int = 0,
    val isEnabled: Boolean = true,
)
