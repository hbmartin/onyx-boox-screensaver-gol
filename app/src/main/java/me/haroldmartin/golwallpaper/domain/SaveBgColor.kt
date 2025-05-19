package me.haroldmartin.golwallpaper.domain

fun interface SaveBgColor {
    suspend operator fun invoke(color: Int)
}
