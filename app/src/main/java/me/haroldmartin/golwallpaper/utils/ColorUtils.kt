package me.haroldmartin.golwallpaper.utils

private const val OPAQUE_ALPHA = 0xFF000000.toInt()
private const val RGB_MASK = 0x00FFFFFF

internal fun Int.inverseRgb(): Int = (this xor RGB_MASK) or OPAQUE_ALPHA
