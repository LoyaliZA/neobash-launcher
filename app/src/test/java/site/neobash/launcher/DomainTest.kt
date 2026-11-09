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
    @Test fun daylightTransitionsAndSolarProgress() {
        val sun = SimulatedCelestialProvider().snapshot().sun
        assertNull(sun.transitProgress(LocalTime.of(6, 29)))
        assertEquals(0f, sun.transitProgress(sun.rise!!)!!, 0f)
        assertNull(sun.transitProgress(sun.set!!))
        assertEquals(.5f, sun.transitProgress(LocalTime.of(12, 37, 30))!!, .0001f)
    }
    @Test fun transitCrossesMidnightAndHandlesUnavailableEvents() {
        val moon = BodyEphemeris(LocalTime.of(18, 0), LocalTime.of(6, 0))
        assertEquals(.5f, moon.transitProgress(LocalTime.MIDNIGHT)!!, .0001f)
        assertNull(moon.transitProgress(LocalTime.NOON))
        assertNull(moon.transitProgress(LocalTime.of(6, 0)))
        assertNull(BodyEphemeris(null, null).transitProgress(LocalTime.NOON))
        assertNull(BodyEphemeris(LocalTime.NOON, LocalTime.NOON).transitProgress(LocalTime.NOON))
    }
    @Test fun simultaneousBodiesUseIndependentTransits() {
        val data = SimulatedCelestialProvider().snapshot()
        val time = LocalTime.of(14, 0)
        val sun = data.sun.transitProgress(time)!!
        val moon = data.moon.transitProgress(time)!!
        assertTrue(sun > moon)
        assertNull(data.sun.transitProgress(LocalTime.of(21, 0)))
        assertNotNull(data.moon.transitProgress(LocalTime.of(21, 0)))
        assertEquals(CelestialSource.SIMULATED, data.source)
    }
    @Test fun semicircleIsSymmetricAndCircular() {
        val left = semicirclePoint(0f)
        val right = semicirclePoint(1f)
        val top = semicirclePoint(.5f)
        assertEquals(-1f, left.x, .00001f)
        assertEquals(1f, right.x, .00001f)
        assertEquals(0f, top.x, .00001f)
        assertEquals(-1f, top.y, .00001f)
        val a = semicirclePoint(.2f)
        val b = semicirclePoint(.8f)
        assertEquals(-a.x, b.x, .00001f)
        assertEquals(a.y, b.y, .00001f)
        assertEquals(1f, a.x*a.x + a.y*a.y, .00001f)
    }
    @Test fun principalMoonPhases() {
        val base = SimulatedCelestialProvider().snapshot()
        listOf(0.0 to 0.0, .25 to .5, .5 to 1.0, .75 to .5, 1.0 to 0.0).forEach { (phase, fraction) ->
            assertEquals(fraction, base.copy(phase = phase).illuminatedFraction, .000001)
        }
    }
}
