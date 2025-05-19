package me.haroldmartin.golwallpaper.domain

fun interface SaveFgColor {
    suspend operator fun invoke(color: Int)
}
