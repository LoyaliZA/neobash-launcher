package site.neobash.launcher.core.model

import java.time.LocalTime
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class CelestialSource { SIMULATED, VERIFIED }

/** Optional measured coordinates are reserved for a future verified provider. */
data class BodyEphemeris(
    val rise: LocalTime?,
    val set: LocalTime?,
    val elevationDegrees: Double? = null,
    val azimuthDegrees: Double? = null,
) {
    /** Stylized horizon transit; null also covers unknown or polar rise/set events. */
    fun transitProgress(time: LocalTime): Float? {
        val start = rise?.toSecondOfDay() ?: return null
        val end = set?.toSecondOfDay() ?: return null
        val duration = (end - start + 86400) % 86400
        if (duration == 0) return null
        val elapsed = (time.toSecondOfDay() - start + 86400) % 86400
        return if (elapsed < duration) elapsed.toFloat() / duration else null
    }
}

data class CelestialData(
    val sun: BodyEphemeris,
    val moon: BodyEphemeris,
    /** Synodic phase: 0=new, .5=full; used for the lit side in the demo. */
    val phase: Double,
    val source: CelestialSource,
    val lunarIlluminatedFraction: Double? = null,
    /** Apparent bright-limb rotation, clockwise in the visual's screen coordinates. */
    val lunarOrientationDegrees: Float? = null,
) {
    val illuminatedFraction: Double get() = lunarIlluminatedFraction?.coerceIn(0.0, 1.0)
        ?: (1 - cos(2 * PI * phase)) / 2
}

interface CelestialProvider { fun snapshot(): CelestialData }
class SimulatedCelestialProvider : CelestialProvider {
    override fun snapshot() = CelestialData(
        sun = BodyEphemeris(LocalTime.of(6, 30), LocalTime.of(18, 45)),
        moon = BodyEphemeris(LocalTime.of(12, 0), LocalTime.of(23, 30)),
        phase = 0.35,
        source = CelestialSource.SIMULATED,
        lunarOrientationDegrees = 0f,
    )
}

data class ArcPoint(val x: Float, val y: Float)
/** Unit semicircle: left horizon (-1,0), zenith (0,-1), right horizon (1,0).
 * Elapsed rise/set transit is a visual convention, not an astronomical projection.
 */
fun semicirclePoint(progress: Float): ArcPoint {
    val angle = PI * (1 - progress.coerceIn(0f, 1f))
    return ArcPoint(cos(angle).toFloat(), -sin(angle).toFloat())
}
