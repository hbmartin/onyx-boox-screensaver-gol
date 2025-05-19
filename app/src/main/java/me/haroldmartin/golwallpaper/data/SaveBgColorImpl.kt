package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.SaveBgColor

class SaveBgColorImpl(val dataStore: UserDataStore) : SaveBgColor {
    override suspend fun invoke(color: Int) {
        dataStore[UserDataStore.Keys.BG_COLOR] = color
    }
}
