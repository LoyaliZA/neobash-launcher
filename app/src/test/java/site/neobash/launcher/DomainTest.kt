package site.neobash.launcher

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalTime
import site.neobash.launcher.core.model.*

class DomainTest {
    @Test fun searchIgnoresAccentsCaseAndOuterWhitespace() {
        val apps = listOf(LaunchableApp("a", "Cámara"), LaunchableApp("b", "Calendario"))
        assertEquals(listOf(apps[0]), filterApps(apps, "  CAMARA "))
        assertEquals(apps, filterApps(apps, ""))
        assertTrue(filterApps(apps, "ausente").isEmpty())
    }
    @Test fun daylightTransitionsAndSolarClamping() {
        val data = SimulatedCelestialProvider().snapshot()
        assertFalse(data.isDay(LocalTime.of(6, 29)))
        assertTrue(data.isDay(data.sunrise))
        assertFalse(data.isDay(data.sunset))
        assertEquals(0f, data.solarProgress(LocalTime.MIDNIGHT), 0f)
        assertEquals(1f, data.solarProgress(LocalTime.MAX), 0f)
        assertEquals(.5f, data.solarProgress(LocalTime.of(12, 37, 30)), .0001f)
    }
    @Test fun principalMoonPhases() {
        val base = SimulatedCelestialProvider().snapshot()
        listOf(0.0 to 0.0, .25 to .5, .5 to 1.0, .75 to .5, 1.0 to 0.0).forEach { (phase, fraction) ->
            assertEquals(fraction, base.copy(phase = phase).illuminatedFraction, .000001)
        }
    }
}
