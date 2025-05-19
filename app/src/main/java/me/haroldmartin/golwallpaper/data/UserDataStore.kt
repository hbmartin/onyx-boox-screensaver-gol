package me.haroldmartin.golwallpaper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val USER_PREFERENCES_NAME = "user_preferences"

@Suppress("PropertyName")
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME,
)

class UserDataStore(private val dataStore: DataStore<Preferences>) {
    constructor(context: Context) : this(
        dataStore = context.dataStore,
    )

    operator fun <T : Any> get(key: Keys<T>): Flow<T?> = dataStore.data.map { it[key.prefsKey] }

    suspend operator fun <T : Any> set(key: Keys<T>, value: T) {
        dataStore.edit { preferences ->
            preferences[key.prefsKey] = value
        }
    }

    @Suppress("ClassName")
    sealed class Keys<T : Any>(val prefsKey: Preferences.Key<T>) {
        object FG_COLOR : Keys<Int>(intPreferencesKey("fg_color"))
        object BG_COLOR : Keys<Int>(intPreferencesKey("bg_color"))
        object GAME_STATE : Keys<String>(stringPreferencesKey("game_state"))
        object PREV_IMAGE_URI : Keys<String>(stringPreferencesKey("prev_image_uri"))
    }
}
