package me.haroldmartin.golwallpaper.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import me.haroldmartin.golwallpaper.domain.CalendarHorizon
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.CalendarSettingsStore
import me.haroldmartin.golwallpaper.domain.OverlayCorner
import me.haroldmartin.golwallpaper.domain.OverlaySize
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val CALENDAR_PREFERENCES_FILE = "calendar_preferences.preferences_pb"

class CalendarPreferences(
    context: Context,
    scope: CoroutineScope,
    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() },
        scope = scope,
        produceFile = { File(context.noBackupFilesDir, CALENDAR_PREFERENCES_FILE) },
    ),
) : CalendarSettingsStore {
    override val settings: Flow<CalendarOverlaySettings> = dataStore.data.map { preferences ->
        CalendarOverlaySettings(
            isEnabled = preferences[Keys.ENABLED] == true,
            horizon = preferences[Keys.HORIZON].toEnumOrDefault(CalendarHorizon.NEXT_7_DAYS),
            corner = preferences[Keys.CORNER].toEnumOrDefault(OverlayCorner.TOP_RIGHT),
            size = preferences[Keys.SIZE].toEnumOrDefault(OverlaySize.MEDIUM),
            selectedCalendarIds = preferences[Keys.SELECTED_IDS]
                .orEmpty()
                .mapNotNull(String::toLongOrNull)
                .toSet(),
        )
    }

    override suspend fun save(settings: CalendarOverlaySettings) {
        dataStore.edit { preferences ->
            preferences[Keys.ENABLED] = settings.isEnabled
            preferences[Keys.HORIZON] = settings.horizon.name
            preferences[Keys.CORNER] = settings.corner.name
            preferences[Keys.SIZE] = settings.size.name
            preferences[Keys.SELECTED_IDS] = settings.selectedCalendarIds.map(Long::toString).toSet()
        }
    }

    private object Keys {
        val ENABLED = booleanPreferencesKey("enabled")
        val HORIZON = stringPreferencesKey("horizon")
        val CORNER = stringPreferencesKey("corner")
        val SIZE = stringPreferencesKey("size")
        val SELECTED_IDS = stringSetPreferencesKey("selected_calendar_ids")
    }
}

private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
    enumValues<T>().firstOrNull { value -> value.name == this } ?: default
