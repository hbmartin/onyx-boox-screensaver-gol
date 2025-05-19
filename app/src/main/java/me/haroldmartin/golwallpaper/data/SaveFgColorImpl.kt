package me.haroldmartin.golwallpaper.data

import me.haroldmartin.golwallpaper.domain.SaveFgColor

class SaveFgColorImpl(val dataStore: UserDataStore) : SaveFgColor {
    override suspend fun invoke(color: Int) {
        dataStore[UserDataStore.Keys.FG_COLOR] = color
    }
}
