package me.haroldmartin.golwallpaper.domain

import java.util.UUID

const val CUSTOM_STARTING_PATTERN = "CUSTOM_RLE"

data class Layer(
    val fgColor: Int = DEFAULT_FG,
    val rule: String = DEFAULT_RULE,
    val state: String? = null,
    val startingPattern: String? = null,
    val generation: Int = 0,
    val isEnabled: Boolean = true,
    val id: String = UUID.randomUUID().toString(),
)
