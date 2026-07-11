package me.haroldmartin.golwallpaper

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.haroldmartin.golwallpaper.data.CalendarPreferences
import me.haroldmartin.golwallpaper.domain.CalendarHorizon
import me.haroldmartin.golwallpaper.domain.CalendarOverlaySettings
import me.haroldmartin.golwallpaper.domain.OverlayCorner
import me.haroldmartin.golwallpaper.domain.OverlaySize
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@RunWith(AndroidJUnit4::class)
@Suppress("InjectDispatcher")
class CalendarPreferencesTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val file = File(context.noBackupFilesDir, "calendar_preferences_test.preferences_pb")
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = scope,
        produceFile = { file },
    )
    private val preferences = CalendarPreferences(context, scope, dataStore)

    @Before
    fun clearFile() {
        file.delete()
    }

    @After
    fun cleanup() {
        scope.cancel()
        file.delete()
    }

    @Test
    fun defaultsAndSettingsRoundTripInNoBackupStorage() {
        runBlocking {
            val defaults = preferences.settings.first()
            assertFalse(defaults.isEnabled)
            assertEquals(CalendarHorizon.NEXT_7_DAYS, defaults.horizon)
            assertTrue(file.canonicalPath.startsWith(context.noBackupFilesDir.canonicalPath))

            val expected = CalendarOverlaySettings(
                isEnabled = true,
                horizon = CalendarHorizon.TODAY,
                corner = OverlayCorner.BOTTOM_LEFT,
                size = OverlaySize.LARGE,
                selectedCalendarIds = setOf(7, 11),
            )
            preferences.save(expected)

            assertEquals(expected, preferences.settings.first())
        }
    }
}
